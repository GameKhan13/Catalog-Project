package musiccatalog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistIntegrationTest {

    @Test
    @DisplayName("IT-05-TB: Create playlist with valid name")
    void createPlaylist_withValidName_succeeds() throws Exception {
        // Arrange
        String username = "u" + (System.currentTimeMillis() % 100000);
        String password = "password123";

        String token = signupAndGetToken(username, password);

        String requestBody = """
                {
                  "name": "My Playlist"
                }
                """;

        HttpURLConnection connection = openPostConnection(
                "http://localhost:4570/api/playlists",
                requestBody,
                token
        );

        // Act
        int status = connection.getResponseCode();
        String response = readResponseBody(connection);

        // Assert
        assertEquals(200, status);
        assertTrue(response.contains("My Playlist"));
    }

    // ---------- helpers ----------
    private String signupAndGetToken(String username, String password) throws Exception {
        String body = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        HttpURLConnection conn = openPostConnection(
                "http://localhost:4570/api/auth/signup",
                body,
                null
        );

        String response = readResponseBody(conn);

        return extractToken(response);
    }

    private String extractToken(String response) {
        return response.split("\"token\":\"")[1].split("\"")[0];
    }

    private HttpURLConnection openPostConnection(String urlStr, String body, String token) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        return conn;
    }

    private String readResponseBody(HttpURLConnection conn) throws Exception {
        InputStream stream = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        if (stream == null) return "";

        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}