package musiccatalog.service;

import musiccatalog.model.User;
import musiccatalog.security.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    @DisplayName("UT-04-CB: SessionService.createSession() creates a non-null token")
    void createSession_withValidUser_returnsNonNullToken() {
        // Arrange: create the session service and a sample user for the test.
        SessionService sessionService = new SessionService();
        User user = new User("1", "emma", "pass", false);

        // Act: create a session token for the sample user.
        String token = sessionService.createSession(user);

        // Assert: the created token should exist and should not be blank.
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

}