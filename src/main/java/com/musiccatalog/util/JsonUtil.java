package com.musiccatalog.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Shared JSON utility class.
 *
 * Instead of creating ObjectMapper objects all over the codebase,
 * the application keeps one configured mapper here and reuses it.
 */
public final class JsonUtil {
    /**
     * Shared Jackson mapper used for converting Java objects to JSON
     * and JSON request bodies back into Java structures.
     */
    public static final ObjectMapper MAPPER = new ObjectMapper()
            // Automatically register standard Java modules when available.
            .findAndRegisterModules()
            // Keep date output readable instead of numeric timestamps.
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Private constructor because this is a utility holder class.
     */
    private JsonUtil() {}
}
