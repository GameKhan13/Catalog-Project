package musiccatalog.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AdminAccessSystemTest {
    // BOTH SYSTEM TESTS REQUIRE THE APPLICATION TO BE RUNNING

    private static final String BASE_URL = "http://localhost:4570";

    private String readBody(InputStream inputStream) throws Exception {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extractToken(String responseBody) {
        int tokenKey = responseBody.indexOf("\"token\"");
        if (tokenKey == -1) {
            return null;
        }

        int colon = responseBody.indexOf(":", tokenKey);
        int firstQuote = responseBody.indexOf("\"", colon + 1);
        int secondQuote = responseBody.indexOf("\"", firstQuote + 1);

        if (firstQuote == -1 || secondQuote == -1) {
            return null;
        }

        return responseBody.substring(firstQuote + 1, secondQuote);
    }

    private HttpURLConnection openJsonConnection(String endpoint, String method, String token) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");

        if (token != null && !token.isBlank()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        connection.setDoOutput(true);
        return connection;
    }

    private String sendJson(HttpURLConnection connection, String jsonBody) throws Exception {
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (stream == null) {
            return "";
        }

        return readBody(stream);
    }

    private String signupAndGetToken(String username, String password) throws Exception {
        HttpURLConnection connection = openJsonConnection("/api/auth/signup", "POST", null);

        String jsonBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        String responseBody = sendJson(connection, jsonBody);

        assertEquals(200, connection.getResponseCode(),
                "Signup should succeed for a new normal user. Response was: " + responseBody);

        String token = extractToken(responseBody);
        assertNotNull(token, "Signup response should contain a token. Response was: " + responseBody);
        assertFalse(token.isBlank(), "Returned signup token should not be blank");

        return token;
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        HttpURLConnection connection = openJsonConnection("/api/auth/login", "POST", null);

        String jsonBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        String responseBody = sendJson(connection, jsonBody);

        assertEquals(200, connection.getResponseCode(),
                "Login should succeed for a valid existing user. Response was: " + responseBody);

        String token = extractToken(responseBody);
        assertNotNull(token, "Login response should contain a token. Response was: " + responseBody);
        assertFalse(token.isBlank(), "Returned login token should not be blank");

        return token;
    }

    @Test
    @DisplayName("ST-06-OB: Non-admin user is denied access to admin-only song creation")
    void createSong_withNonAdminUser_returnsAccessDenied() throws Exception {
        String username = "u" + (System.currentTimeMillis() % 100000);
        String token = signupAndGetToken(username, "password123");

        HttpURLConnection connection = openJsonConnection("/api/admin/songs", "POST", token);

        String jsonBody = """
                {
                  "title": "Blocked Song",
                  "artist": "Blocked Artist",
                  "album": "Blocked Album",
                  "year": 2026,
                  "genre": "Test",
                  "lyrics": "This should fail",
                  "imageFile": "default.svg"
                }
                """;

        String responseBody = sendJson(connection, jsonBody);

        assertEquals(403, connection.getResponseCode(),
                "Non-admin users should be denied admin-only actions. Response was: " + responseBody);
    }

    @Test
    @DisplayName("ST-07-OB: Admin user can successfully create a song")
    void createSong_withAdminUser_succeeds() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        HttpURLConnection connection = openJsonConnection("/api/admin/songs", "POST", token);

        String jsonBody = """
                {
                  "title": "Test Song",
                  "artist": "Test Artist",
                  "album": "Test Album",
                  "year": 2026,
                  "genre": "Test",
                  "lyrics": "This should succeed",
                  "imageFile": "default.svg"
                }
                """;

        String responseBody = sendJson(connection, jsonBody);

        assertTrue(connection.getResponseCode() == 200 || connection.getResponseCode() == 201,
                "Admin users should be allowed to perform admin-only actions. Response was: " + responseBody);
    }
}