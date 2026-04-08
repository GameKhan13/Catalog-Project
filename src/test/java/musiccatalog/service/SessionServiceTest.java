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
    @Test
    @DisplayName("UT-05-CB: SessionService.getUser() returns the correct user for a valid token")
    void getUser_withValidToken_returnsUser() {
        // Arrange: create the session service and a sample user, then create a session token.
        SessionService sessionService = new SessionService();
        User user = new User("1", "Emma", "pass", false);

        String token = sessionService.createSession(user);

        // Act: retrieve the user associated with the generated token.
        var result = sessionService.getUser(token);

        // Assert: the result should contain the correct user.
        assertTrue(result.isPresent());
        assertEquals("Emma", result.get().getUsername());
    }
    @Test
    @DisplayName("UT-06-CB: SessionService.getUser() returns empty for an invalid token")
    void getUser_withInvalidToken_returnsEmpty() {
        // Arrange: create the session service with no valid sessions.
        SessionService sessionService = new SessionService();

        // Act: attempt to retrieve a user using a fake/invalid token.
        var result = sessionService.getUser("invalid-token");

        // Assert: no user should be found for an invalid token.
        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("UT-07-CB: SessionService.remove() invalidates an existing session token")
    void remove_withValidToken_removesSession() {
        // Arrange: create the session service and a user, then generate a session token.
        SessionService sessionService = new SessionService();
        User user = new User("1", "emma", "pass", false);

        String token = sessionService.createSession(user);

        // Act: remove (invalidate) the session token.
        sessionService.remove(token);

        // Assert: the token should no longer return a user.
        var result = sessionService.getUser(token);
        assertTrue(result.isEmpty());
    }
}