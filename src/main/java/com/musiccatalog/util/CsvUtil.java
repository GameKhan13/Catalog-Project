package com.musiccatalog.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for converting playlist song id lists
 * to and from the CSV storage format.
 */
public final class CsvUtil {
    /**
     * Private constructor because this is a utility class.
     */
    private CsvUtil() {}

    /**
     * Joins a list of ids into a single pipe-separated String.
     *
     * Example:
     * ["a", "b", "c"] -> "a|b|c"
     */
    public static String joinIds(List<String> ids) {
        // Null or empty lists become an empty String in the CSV file.
        if (ids == null || ids.isEmpty()) {
            return "";
        }

        // The pipe character is used as the separator inside one CSV column.
        return String.join("|", ids);
    }

    /**
     * Splits a pipe-separated String back into a list of ids.
     *
     * Example:
     * "a|b|c" -> ["a", "b", "c"]
     */
    public static List<String> splitIds(String raw) {
        List<String> ids = new ArrayList<>();

        // Empty input means there are no stored ids.
        if (raw == null || raw.isBlank()) {
            return ids;
        }

        // Break the stored value apart on each pipe.
        for (String value : raw.split("\\|")) {
            // Trim each value so accidental spaces do not become part of the id.
            String trimmed = value.trim();

            // Skip blank pieces in case the stored data is messy.
            if (!trimmed.isEmpty()) {
                ids.add(trimmed);
            }
        }

        return ids;
    }
}
