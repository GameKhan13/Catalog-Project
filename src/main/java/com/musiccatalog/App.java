package com.musiccatalog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.musiccatalog.security.SessionService;
import com.musiccatalog.service.DataBootstrapService;
import com.musiccatalog.service.PlaylistService;
import com.musiccatalog.service.SongService;
import com.musiccatalog.service.UserService;
import com.musiccatalog.util.JsonUtil;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

/**
 * Main entry point for the application.
 *
 * This class wires together the services and registers the HTTP routes
 * using the Spark Java framework.
 */
public class App {
    /**
     * Starts the web server and defines all routes.
     */
    public static void main(String[] args) {
        // Configure the HTTP port the server will listen on.
        port(4568);

        // Tell Spark to serve the front-end files from src/main/resources/public.
        staticFiles.location("/public");

        // Create the service objects used throughout the application.
        UserService userService = new UserService();
        SongService songService = new SongService();
        PlaylistService playlistService = new PlaylistService();
        SessionService sessionService = new SessionService();

        // Prepare the CSV files, seed data, and image assets before serving requests.
        DataBootstrapService bootstrapService = new DataBootstrapService(userService, songService, playlistService);
        bootstrapService.initialize();

        // Force all normal responses to default to JSON unless a route overrides it.
        before((req, res) -> res.type("application/json"));

        // Simple health-check route used to confirm the server is running.
        get("/api/health", (req, res) -> json(Map.of("ok", true, "stage", "catalog-browser")));

        // Route that serves cover-image files stored in the data/images folder.
        get("/song-images/:file", (req, res) -> {
            // Read the requested file name from the route parameter.
            String fileName = req.params("file");

            // Resolve the file safely under the configured image directory.
            Path imagePath = DataBootstrapService.IMAGE_DIR.resolve(fileName).normalize();

            // Fall back to the default image if the file does not exist
            // or if path normalization would escape the intended image folder.
            if (!Files.exists(imagePath) || !imagePath.startsWith(DataBootstrapService.IMAGE_DIR)) {
                imagePath = DataBootstrapService.IMAGE_DIR.resolve("default.svg");
            }

            // Set the response Content-Type based on the file extension.
            String lower = imagePath.getFileName().toString().toLowerCase();
            if (lower.endsWith(".svg")) {
                res.type("image/svg+xml");
            } else if (lower.endsWith(".png")) {
                res.type("image/png");
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                res.type("image/jpeg");
            } else if (lower.endsWith(".webp")) {
                res.type("image/webp");
            } else {
                // Generic fallback for unknown file types.
                res.type("application/octet-stream");
            }

            // Return the raw bytes of the image file.
            return Files.readAllBytes(imagePath);
        });

        // Sign-up route for creating a new normal user account.
        post("/api/auth/signup", (req, res) -> {
            // Convert the JSON request body into a Map.
            Map<String, Object> body = bodyMap(req.body());

            // Read and sanitize the username and password fields.
            String username = safe(body.get("username"));
            String password = safe(body.get("password"));

            // Apply basic validation rules.
            validateUsername(username);
            validatePassword(password);

            // Reject duplicate usernames.
            if (userService.findByUsername(username).isPresent()) {
                haltWithJson(409, "Username already exists");
            }

            // Store a hashed password, never the raw password.
            User user = userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);

            // Create a logged-in session immediately after sign-up.
            String token = sessionService.createSession(user);

            return json(authPayload(user, token));
        });

        // Login route for existing users.
        post("/api/auth/login", (req, res) -> {
            Map<String, Object> body = bodyMap(req.body());
            String username = safe(body.get("username"));
            String password = safe(body.get("password"));

            // Look up the user by username.
            Optional<User> maybeUser = userService.findByUsername(username);

            // Fail when the username does not exist or the password hash does not match.
            if (maybeUser.isEmpty() || !BCrypt.checkpw(password, maybeUser.get().getPasswordHash())) {
                haltWithJson(401, "Invalid username or password");
            }

            User user = maybeUser.get();

            // Create a fresh session token for the logged-in user.
            String token = sessionService.createSession(user);

            return json(authPayload(user, token));
        });

        // Logout route removes the caller's session token from memory.
        post("/api/auth/logout", (req, res) -> {
            sessionService.remove(extractToken(req.headers("Authorization")));
            return json(Map.of("message", "Logged out"));
        });

        // Route that returns the currently authenticated user's public fields.
        get("/api/auth/me", (req, res) -> json(publicUser(requireUser(req.headers("Authorization"), sessionService))));

        // Route that returns the full song catalog.
        get("/api/songs", (req, res) -> json(songService.getAllSongs()));

        // Global error handler so unexpected exceptions become JSON instead of HTML.
        exception(Exception.class, (exception, req, res) -> {
            // If no error status has been set yet, default to 500.
            if (res.status() < 400) {
                res.status(500);
            }

            res.type("application/json");

            try {
                // Return the actual message when possible for easier debugging.
                res.body(json(Map.of("error", exception.getMessage() == null ? "Unexpected error" : exception.getMessage())));
            } catch (Exception ignored) {
                // Fall back to a simple hard-coded JSON string if serialization fails.
                res.body("{\"error\":\"Unexpected error\"}");
            }
        });
    }

    /**
     * Ensures that the request contains a valid session token.
     *
     * @return the authenticated user
     */
    private static User requireUser(String authorizationHeader, SessionService sessionService) {
        // Try to resolve the user from the supplied token.
        User user = sessionService.getUser(extractToken(authorizationHeader)).orElse(null);

        // Stop the request immediately if the token is missing or invalid.
        if (user == null) {
            haltWithJson(401, "Unauthorized");
        }

        return user;
    }

    /**
     * Ensures that the authenticated user is an administrator.
     */
    private static User requireAdmin(String authorizationHeader, SessionService sessionService) {
        User user = requireUser(authorizationHeader, sessionService);

        if (!user.isAdmin()) {
            haltWithJson(403, "Admin access required");
        }

        return user;
    }

    /**
     * Extracts the raw token from the Authorization header.
     *
     * Supports either:
     * - "Bearer actual-token"
     * - "actual-token"
     */
    private static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }

        return authorizationHeader.trim();
    }

    /**
     * Converts a request body Map into a validated Song object for creation.
     */
    private static Song parseSongForCreate(Map<String, Object> body) {
        String title = safe(body.get("title"));
        String artist = safe(body.get("artist"));
        String album = safe(body.get("album"));
        String genre = safe(body.get("genre"));
        String lyrics = safe(body.get("lyrics"));
        String imageFile = safe(body.get("imageFile"));
        int year = parseInt(body.get("year"));

        // Title and artist are treated as required fields.
        if (title.isBlank() || artist.isBlank()) {
            haltWithJson(400, "Title and artist are required");
        }

        // Keep the year within a simple valid range.
        if (year < 0 || year > 9999) {
            haltWithJson(400, "Year must be between 0 and 9999");
        }

        // Use a default image if the client does not provide one.
        if (imageFile.isBlank()) {
            imageFile = "default.svg";
        }

        return new Song(null, title, artist, album, year, genre, lyrics, imageFile);
    }

    /**
     * Validates the username format.
     */
    private static void validateUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            haltWithJson(400, "Username must be 3-20 characters");
        }

        if (!username.matches("^[A-Za-z0-9_]+$")) {
            haltWithJson(400, "Username can only contain letters, numbers, and underscores");
        }
    }

    /**
     * Validates password length.
     */
    private static void validatePassword(String password) {
        if (password.length() < 6) {
            haltWithJson(400, "Password must be at least 6 characters");
        }
    }

    /**
     * Safely parses an integer value from an object.
     *
     * Returns -1 when parsing fails so validation can reject it later.
     */
    private static int parseInt(Object value) {
        try {
            return Integer.parseInt(safe(value));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Parses a JSON request body into a Map.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> bodyMap(String body) {
        try {
            if (body == null || body.isBlank()) {
                return new HashMap<>();
            }

            return JsonUtil.MAPPER.readValue(body, Map.class);
        } catch (Exception e) {
            haltWithJson(400, "Invalid JSON body");
            return new HashMap<>();
        }
    }

    /**
     * Converts null values to an empty trimmed String.
     */
    private static String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    /**
     * Builds the standard authentication response payload.
     */
    private static Map<String, Object> authPayload(User user, String token) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("user", publicUser(user));
        return payload;
    }

    /**
     * Returns only the user fields that should be exposed publicly.
     *
     * The password hash is intentionally left out.
     */
    private static Map<String, Object> publicUser(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "isAdmin", user.isAdmin()
        );
    }

    /**
     * Stops request processing and returns a JSON error payload.
     */
    private static void haltWithJson(int status, String message) {
        halt(status, json(Map.of("error", message)));
    }

    /**
     * Serializes an object into JSON using the shared ObjectMapper.
     */
    private static String json(Object object) {
        try {
            return JsonUtil.MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON", e);
        }
    }

}
