package com.musiccatalog.util;

import java.util.UUID;

/**
 * Small helper for generating ids used across the application.
 */
public final class IdUtil {
    /**
     * Private constructor because this class should never be instantiated.
     */
    private IdUtil() {}

    /**
     * Creates a new random UUID String.
     *
     * The application uses these ids for users, songs, and playlists.
     */
    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
