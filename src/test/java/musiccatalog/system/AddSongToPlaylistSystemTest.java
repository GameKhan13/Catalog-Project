package musiccatalog.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AddSongToPlaylistSystemTest {

    private static final String BASE_URL = "http://localhost:4570";

    @Test
    @DisplayName("ST-04-OB: User adds song to playlist")
    void userAddsSongToPlaylist_succeeds() throws Exception {
        String username = "u" + (System.currentTimeMillis() % 100000);
        String password = "password123";

        String token = signupAndGetToken(username, password);
        String playlistId = createPlaylist(token);

        HttpURLConnection connection = openPost(
                BASE_URL + "/api/playlists/" + playlistId + "/songs",
                "{ \"songId\": \"1\" }",
                token
        );

        int status = connection.getResponseCode();
        String response = read(connection);

        assertEquals(200, status, "Adding a song to the playlist should succeed");
        assertTrue(response.contains("1"), "Response should show that song ID 1 was added");
    }

    private String signupAndGetToken(String username, String password) throws Exception {
        String body = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        HttpURLConnection connection = openPost(BASE_URL + "/api/auth/signup", body, null);
        int status = connection.getResponseCode();
        String response = read(connection);

        assertEquals(200, status, "Signup should succeed. Response was: " + response);
        return extractJsonValue(response, "token");
    }

    private String createPlaylist(String token) throws Exception {
        HttpURLConnection connection = openPost(
                BASE_URL + "/api/playlists",
                "{ \"name\": \"System Test Playlist\" }",
                token
        );

        int status = connection.getResponseCode();
        String response = read(connection);

        assertEquals(200, status, "Playlist creation should succeed. Response was: " + response);
        return extractJsonValue(response, "id");
    }

    private HttpURLConnection openPost(String urlStr, String body, String token) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        if (token != null && !token.isBlank()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        return connection;
    }

    private String read(HttpURLConnection connection) throws Exception {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (stream == null) {
            return "";
        }

        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
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