package musiccatalog.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RenamePlaylistSystemTest {

    private static final String BASE_URL = "http://localhost:4570";

    @Test
    @DisplayName("ST-05-OB: Rename playlist")
    void renamePlaylist_succeeds() throws Exception {
        String username = "zak" + System.currentTimeMillis() % 10000;
        String password = "password123";

        String token = signupAndGetToken(username, password);
        String playlistId = createPlaylist(token);

        HttpURLConnection conn = (HttpURLConnection)
                new URL(BASE_URL + "/api/playlists/" + playlistId).openConnection();

        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);

        String body = "{ \"name\": \"Updated Playlist\" }";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        String response = read(conn);

        assertEquals(200, status, "Rename playlist failed. Response was: " + response);
    }

    // ---------- helpers ----------

    private String signupAndGetToken(String username, String password) throws Exception {
        String body = "{ \"username\":\"" + username + "\", \"password\":\"" + password + "\" }";

        HttpURLConnection conn = openPost(
                BASE_URL + "/api/auth/signup",
                body,
                null
        );

        int status = conn.getResponseCode();
        String response = read(conn);

        assertEquals(200, status, "Signup failed. Response was: " + response);
        assertTrue(response.contains("\"token\":"), "Signup response did not contain token. Response was: " + response);

        return extractJsonValue(response, "token");
    }

    private String createPlaylist(String token) throws Exception {
        HttpURLConnection conn = openPost(
                BASE_URL + "/api/playlists",
                "{ \"name\": \"Initial Playlist\" }",
                token
        );

        int status = conn.getResponseCode();
        String response = read(conn);

        assertEquals(200, status, "Create playlist failed. Response was: " + response);
        assertTrue(response.contains("\"id\":"), "Create playlist response did not contain id. Response was: " + response);

        return extractJsonValue(response, "id");
    }

    private HttpURLConnection openPost(String urlStr, String body, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null && !token.isBlank()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
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