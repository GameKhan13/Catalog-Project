package musiccatalog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class LoginIntegrationTest {

    @Test
    @DisplayName("IT-04-OB: Login route succeeds and returns token for valid user")
    void login_withValidExistingUser_returnsToken() throws Exception {
        // Arrange: first sign up a real user so the account exists
        String username = "u" + (System.currentTimeMillis() % 100000);
        String password = "password123";

        signupUser(username, password);

        String loginBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        HttpURLConnection connection = openPostConnection("http://localhost:4570/api/auth/login", loginBody);

        // Act
        int status = connection.getResponseCode();
        String responseBody = readResponseBody(connection);

        // Assert
        assertEquals(200, status, "Login should succeed for a valid existing user");
        assertTrue(responseBody.contains("\"token\""), "Login response should contain a token");
        assertTrue(responseBody.contains("\"user\""), "Login response should contain a user object");
        assertTrue(responseBody.contains("\"username\":\"" + username + "\""),
                "Login response should include the logged in username");
    }

    private void signupUser(String username, String password) throws Exception {
        String signupBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        HttpURLConnection connection = openPostConnection("http://localhost:4570/api/auth/signup", signupBody);
        assertEquals(200, connection.getResponseCode(), "Precondition signup should succeed");
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