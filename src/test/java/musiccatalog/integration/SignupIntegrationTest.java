package musiccatalog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class SignupIntegrationTest {

    @Test
    @DisplayName("IT-03-TB: Signup route creates user and returns token")
    void signup_withNewUsername_createsUserAndReturnsToken() throws Exception {
        // Arrange
        String username = "u" + (System.currentTimeMillis() % 100000);
        String password = "password123";
        String requestBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        HttpURLConnection connection = openPostConnection("http://localhost:4570/api/auth/signup", requestBody);

        // Act
        int status = connection.getResponseCode();
        String responseBody = readResponseBody(connection);

        // Assert
        assertEquals(200, status, "Signup should succeed for a brand new username");
        assertTrue(responseBody.contains("\"token\""), "Signup response should contain a token");
        assertTrue(responseBody.contains("\"user\""), "Signup response should contain a user object");
        assertTrue(responseBody.contains("\"username\":\"" + username + "\""),
                "Signup response should include the created username");
    }

    private HttpURLConnection openPostConnection(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        return connection;
    }

    private String readResponseBody(HttpURLConnection connection) throws Exception {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (stream == null) {
            return "";
        }

        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}