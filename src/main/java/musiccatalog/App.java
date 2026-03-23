package musiccatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import musiccatalog.model.Playlist;
import musiccatalog.model.Song;
import musiccatalog.model.User;
import musiccatalog.security.SessionService;
import musiccatalog.service.DataBootstrapService;
import musiccatalog.service.PlaylistService;
import musiccatalog.service.SongService;
import musiccatalog.service.UserService;
import musiccatalog.util.IdUtil;
import musiccatalog.util.JsonUtil;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

public class App {
    public static void main(String[] args) {
        port(4570);
        staticFiles.location("/public");

        UserService userService = new UserService();
        SongService songService = new SongService();
        PlaylistService playlistService = new PlaylistService();
        SessionService sessionService = new SessionService();
        DataBootstrapService bootstrapService = new DataBootstrapService(userService, songService, playlistService);
        bootstrapService.initialize();

        before((req, res) -> res.type("application/json"));

        get("/api/health", (req, res) -> json(Map.of("ok", true)));

        get("/song-images/:file", (req, res) -> {
            String fileName = req.params("file");
            Path imagePath = DataBootstrapService.IMAGE_DIR.resolve(fileName).normalize();
            if (!Files.exists(imagePath) || !imagePath.startsWith(DataBootstrapService.IMAGE_DIR)) {
                imagePath = DataBootstrapService.IMAGE_DIR.resolve("default.svg");
            }
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
                res.type("application/octet-stream");
            }
            return Files.readAllBytes(imagePath);
        });

        post("/api/auth/signup", (req, res) -> {
            Map<String, Object> body = bodyMap(req.body());
            String username = safe(body.get("username"));
            String password = safe(body.get("password"));

            validateUsername(username);
            validatePassword(password);

            if (userService.findByUsername(username).isPresent()) {
                haltWithJson(409, "Username already exists");
            }

            User user = userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);
            String token = sessionService.createSession(user);
            return json(authPayload(user, token));
        });

        post("/api/auth/login", (req, res) -> {
            Map<String, Object> body = bodyMap(req.body());
            String username = safe(body.get("username"));
            String password = safe(body.get("password"));

            Optional<User> maybeUser = userService.findByUsername(username);
            if (maybeUser.isEmpty() || !BCrypt.checkpw(password, maybeUser.get().getPasswordHash())) {
                haltWithJson(401, "Invalid username or password");
            }

            User user = maybeUser.get();
            String token = sessionService.createSession(user);
            return json(authPayload(user, token));
        });

        post("/api/auth/logout", (req, res) -> {
            sessionService.remove(extractToken(req.headers("Authorization")));
            return json(Map.of("message", "Logged out"));
        });

        get("/api/auth/me", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            return json(publicUser(user));
        });

        get("/api/songs", (req, res) -> json(songService.getAllSongs()));

        post("/api/admin/songs", (req, res) -> {
            requireAdmin(req.headers("Authorization"), sessionService);
            Song song = parseSongForCreate(bodyMap(req.body()));
            song.setId(IdUtil.newId());
            songService.createSong(song);
            return json(song);
        });

        put("/api/admin/songs/:id", (req, res) -> {
            requireAdmin(req.headers("Authorization"), sessionService);
            Song updatedSong = parseSongForCreate(bodyMap(req.body()));
            Song saved = songService.updateSong(req.params("id"), updatedSong);
            return json(saved);
        });

        delete("/api/admin/songs/:id", (req, res) -> {
            requireAdmin(req.headers("Authorization"), sessionService);
            String songId = req.params("id");
            songService.deleteSong(songId);
            playlistService.removeSongFromAllPlaylists(songId);
            return json(Map.of("message", "Song deleted"));
        });

        get("/api/playlists", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            return json(playlistService.getVisiblePlaylistsForUser(user.getId()));
        });

        post("/api/playlists", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            Map<String, Object> body = bodyMap(req.body());
            String name = safe(body.get("name"));
            boolean isGlobal = Boolean.parseBoolean(safe(body.get("isGlobal")));
            if (name.length() < 2) {
                haltWithJson(400, "Playlist name must be at least 2 characters");
            }
            if (isGlobal && !user.isAdmin()) {
                haltWithJson(403, "Only admins can create global playlists");
            }
            Playlist playlist = playlistService.createPlaylist(name, user.getId(), isGlobal);
            return json(playlist);
        });

        put("/api/playlists/:id", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            Playlist playlist = requireOwnedPlaylist(req.params("id"), user, playlistService);
            Map<String, Object> body = bodyMap(req.body());
            String name = safe(body.get("name"));
            if (name.length() < 2) {
                haltWithJson(400, "Playlist name must be at least 2 characters");
            }
            return json(playlistService.renamePlaylist(playlist.getId(), name));
        });

        delete("/api/playlists/:id", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            Playlist playlist = requireOwnedPlaylist(req.params("id"), user, playlistService);
            playlistService.deletePlaylist(playlist.getId());
            return json(Map.of("message", "Playlist deleted"));
        });

        post("/api/playlists/:id/songs", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            Playlist playlist = requireOwnedPlaylist(req.params("id"), user, playlistService);
            Map<String, Object> body = bodyMap(req.body());
            String songId = safe(body.get("songId"));
            if (songService.findById(songId).isEmpty()) {
                haltWithJson(404, "Song not found");
            }
            return json(playlistService.addSong(playlist.getId(), songId));
        });

        delete("/api/playlists/:id/songs/:songId", (req, res) -> {
            User user = requireUser(req.headers("Authorization"), sessionService);
            Playlist playlist = requireOwnedPlaylist(req.params("id"), user, playlistService);
            return json(playlistService.removeSong(playlist.getId(), req.params("songId")));
        });

        exception(Exception.class, (exception, req, res) -> {
            if (res.status() < 400) {
                res.status(500);
            }
            res.type("application/json");
            try {
                res.body(json(Map.of("error", exception.getMessage() == null ? "Unexpected error" : exception.getMessage())));
            } catch (Exception ignored) {
                res.body("{\"error\":\"Unexpected error\"}");
            }
        });
    }

    private static Playlist requireOwnedPlaylist(String playlistId, User user, PlaylistService playlistService) throws IOException {
        Playlist playlist = playlistService.findById(playlistId).orElse(null);
        if (playlist == null) {
            haltWithJson(404, "Playlist not found");
        }
        boolean allowed = playlist.isGlobal() ? user.isAdmin() : playlist.getOwnerUserId().equals(user.getId());
        if (!allowed) {
            haltWithJson(403, playlist.isGlobal() ? "Only admins can edit global playlists" : "You do not own this playlist");
        }
        return playlist;
    }

    private static User requireUser(String authorizationHeader, SessionService sessionService) {
        User user = sessionService.getUser(extractToken(authorizationHeader)).orElse(null);
        if (user == null) {
            haltWithJson(401, "Unauthorized");
        }
        return user;
    }

    private static User requireAdmin(String authorizationHeader, SessionService sessionService) {
        User user = requireUser(authorizationHeader, sessionService);
        if (!user.isAdmin()) {
            haltWithJson(403, "Admin access required");
        }
        return user;
    }

    private static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }
        return authorizationHeader.trim();
    }

    private static Song parseSongForCreate(Map<String, Object> body) {
        String title = safe(body.get("title"));
        String artist = safe(body.get("artist"));
        String album = safe(body.get("album"));
        String genre = safe(body.get("genre"));
        String lyrics = safe(body.get("lyrics"));
        String imageFile = safe(body.get("imageFile"));
        int year = parseInt(body.get("year"));

        if (title.isBlank() || artist.isBlank()) {
            haltWithJson(400, "Title and artist are required");
        }
        if (year < 0 || year > 9999) {
            haltWithJson(400, "Year must be between 0 and 9999");
        }
        if (imageFile.isBlank()) {
            imageFile = "default.svg";
        }
        return new Song(null, title, artist, album, year, genre, lyrics, imageFile);
    }

    private static void validateUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            haltWithJson(400, "Username must be 3-20 characters");
        }
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            haltWithJson(400, "Username can only contain letters, numbers, and underscores");
        }
    }

    private static void validatePassword(String password) {
        if (password.length() < 6) {
            haltWithJson(400, "Password must be at least 6 characters");
        }
    }

    private static int parseInt(Object value) {
        try {
            return Integer.parseInt(safe(value));
        } catch (Exception e) {
            return -1;
        }
    }

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

    private static String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static Map<String, Object> authPayload(User user, String token) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("user", publicUser(user));
        return payload;
    }

    private static Map<String, Object> publicUser(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "isAdmin", user.isAdmin()
        );
    }

    private static void haltWithJson(int status, String message) {
        halt(status, json(Map.of("error", message)));
    }

    private static String json(Object object) {
        try {
            return JsonUtil.MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON", e);
        }
    }
}
