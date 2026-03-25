package com.musiccatalog.model;

/**
 * Simple model class that represents one application user.
 *
 * This class does not contain business logic.
 * It only stores data that is read from the users CSV file
 * and later passed around the application.
 */
public class User {
    /**
     * Unique identifier for the user.
     */
    private String id;

    /**
     * Public username used for login.
     */
    private String username;

    /**
     * Stored hashed password, not the plain-text password.
     */
    private String passwordHash;

    /**
     * Flag that tells the application whether this user is an administrator.
     */
    private boolean admin;

    /**
     * No-argument constructor.
     *
     * This is useful for libraries and serializers that need to create an
     * empty object first and then populate its fields later.
     */
    public User() {}

    /**
     * Full constructor used when all user data is already known.
     *
     * @param id unique user id
     * @param username login name
     * @param passwordHash hashed password value
     * @param admin true if the user has admin privileges
     */
    public User(String id, String username, String passwordHash, boolean admin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.admin = admin;
    }

    /**
     * Returns the user's unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the user's unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the username used by the user to sign in.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Updates the username field.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the hashed password.
     *
     * The application stores the password hash rather than the raw password.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Updates the stored password hash.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns whether the user is an administrator.
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Updates the administrator flag.
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
