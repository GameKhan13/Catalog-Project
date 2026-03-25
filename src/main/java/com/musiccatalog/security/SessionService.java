package com.musiccatalog.security;

import com.musiccatalog.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session manager.
 *
 * This class maps randomly generated tokens to User objects so the API can
 * remember who is currently logged in.
 *
 * Important note:
 * sessions are kept only in memory, so restarting the server clears them.
 */
public class SessionService {
    /**
     * Thread-safe map of session token -> user.
     */
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new session token for the given user.
     *
     * @param user the authenticated user
     * @return the generated token that the client will send back later
     */
    public String createSession(User user) {
        // Generate a hard-to-guess token value.
        String token = UUID.randomUUID().toString();

        // Store the token so future requests can be matched to this user.
        sessions.put(token, user);

        return token;
    }

    /**
     * Looks up the logged-in user for a token.
     *
     * @param token session token from the Authorization header
     * @return Optional containing the user if the session exists
     */
    public Optional<User> getUser(String token) {
        // Empty or blank tokens are treated as missing authentication.
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        // Return the matching user if one exists in the session map.
        return Optional.ofNullable(sessions.get(token));
    }

    /**
     * Removes a session token, effectively logging the user out.
     *
     * @param token session token to remove
     */
    public void remove(String token) {
        // Only remove when a token was actually provided.
        if (token != null) {
            sessions.remove(token);
        }
    }
}
