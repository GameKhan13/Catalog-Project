package musiccatalog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistSongIntegrationTest {

    @Test
    @DisplayName("IT-06-TB: Add song to playlist")
    void addSong_toPlaylist_succeeds() throws Exception {

        String username = "u" + (System.currentTimeMillis() % 100000);
        String password = "password123";

        String token = signupAndGetToken(username, password);

        // create playlist
        String playlistRes = post("/api/playlists", "{ \"name\": \"Test\" }", token);
        String playlistId = extractId(playlistRes);

        // add song
        HttpURLConnection conn = openPost(
                "http://localhost:4570/api/playlists/" + playlistId + "/songs",
                "{ \"songId\": \"1\" }",
                token
        );

        assertEquals(200, conn.getResponseCode());
    }

    // -------- helpers --------
    private String signupAndGetToken(String u, String p) throws Exception {
        String res = post("/api/auth/signup",
                "{ \"username\":\"" + u + "\", \"password\":\"" + p + "\" }",
                null
        );
        return extractToken(res);
    }

    private String post(String endpoint, String body, String token) throws Exception {
        HttpURLConnection conn = openPost("http://localhost:4570" + endpoint, body, token);
        return read(conn);
    }

    private String extractToken(String res) {
        return res.split("\"token\":\"")[1].split("\"")[0];
    }

    private String extractId(String res) {
        return res.split("\"id\":\"")[1].split("\"")[0];
    }

    private HttpURLConnection openPost(String urlStr, String body, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();

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

    private String read(HttpURLConnection conn) throws Exception {
        InputStream s = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (s == null) return "";
        return new String(s.readAllBytes(), StandardCharsets.UTF_8);
    }
}