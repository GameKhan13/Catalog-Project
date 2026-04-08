package musiccatalog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AdminDeleteIntegrationTest {

    private static final String BASE_URL = "http://localhost:4570";

    @Test
    @DisplayName("IT-07-OB: Admin deletes song")
    void adminDeleteSong_succeeds() throws Exception {
        String adminUsername = "adm" + (int) (Math.random() * 10000);
        String adminPassword = "password123";

        String adminToken = loginAndGetToken("admin", "admin123");
        String songId = createSongAsAdmin(adminToken);

        HttpURLConnection conn = (HttpURLConnection)
                new URL(BASE_URL + "/api/admin/songs/" + songId).openConnection();

        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + adminToken);

        int status = conn.getResponseCode();
        String response = read(conn);

        assertEquals(200, status, "Admin delete failed. Response was: " + response);
    }

    // ---------- helpers ----------

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = "{ \"username\":\"" + username + "\", \"password\":\"" + password + "\" }";

        HttpURLConnection conn = openConnection(BASE_URL + "/api/auth/login", "POST", body, null);

        int status = conn.getResponseCode();
        String response = read(conn);

        assertEquals(200, status, "Login failed. Response was: " + response);

        return extractJsonValue(response, "token");
    }

    private String createSongAsAdmin(String token) throws Exception {
        HttpURLConnection conn = openConnection(
                BASE_URL + "/api/admin/songs",
                "POST",
                """
                {
                  "title": "Delete Me",
                  "artist": "Test Artist",
                  "album": "Test Album",
                  "year": 2024,
                  "genre": "Test",
                  "lyrics": "Test lyrics",
                  "imageFile": "default.svg"
                }
                """,
                token
        );

        int status = conn.getResponseCode();
        String response = read(conn);

        assertEquals(200, status, "Create song failed. Response was: " + response);
        assertTrue(response.contains("\"id\":"), "No song id in create-song response: " + response);

        return extractJsonValue(response, "id");
    }

    private HttpURLConnection openConnection(String urlStr, String method, String body, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null && !token.isBlank()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        return conn;
    }

    private String read(HttpURLConnection conn) throws Exception {
        InputStream s = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (s == null) return "";
        return new String(s.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        assertTrue(start >= 0, "Could not find key '" + key + "' in response: " + json);

        start += marker.length();
        int end = json.indexOf("\"", start);
        assertTrue(end >= 0, "Could not parse value for key '" + key + "' in response: " + json);

        return json.substring(start, end);
    }
}