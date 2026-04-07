package musiccatalog.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the small utility helpers listed in the assignment table.
 *
 * Each test method includes the matching test ID in the display name so the
 * mapping from the assignment sheet to the code is easy to see.
 */
class UtilityTests {

    @Test
    @DisplayName("UT-01-CB: CsvUtil.joinIds() joins IDs with pipe separators")
    void joinIds_withThreeIds_returnsPipeSeparatedString() {
        // Arrange: build the same input list shown in the test table.
        List<String> ids = List.of("s1", "s2", "s3");

        // Act: join the IDs into the CSV storage format used by the project.
        String joined = CsvUtil.joinIds(ids);

        // Assert: the method preserves order and uses the expected delimiter.
        assertEquals("s1|s2|s3", joined);
    }

    @Test
    @DisplayName("UT-02-CB: CsvUtil.splitIds() splits a pipe-separated string into a list")
    void splitIds_withPipeSeparatedString_returnsListWithThreeIds() {
        // Arrange: use the exact input from the assignment table.
        String rawIds = "s1|s2|s3";

        // Act: split the raw CSV field into individual IDs.
        List<String> ids = CsvUtil.splitIds(rawIds);

        // Assert: the list has the expected size, content, and order.
        assertEquals(3, ids.size());
        assertEquals(List.of("s1", "s2", "s3"), ids);
    }

    @Test
    @DisplayName("UT-03-CB: IdUtil.newId() returns a unique, non-null ID string")
    void newId_returnsUniqueNonNullIdString() {
        // Act: generate two IDs so uniqueness can be checked.
        String firstId = IdUtil.newId();
        String secondId = IdUtil.newId();

        // Assert: both IDs exist, are not blank, and are different from each other.
        assertNotNull(firstId);
        assertNotNull(secondId);
        assertFalse(firstId.isBlank());
        assertFalse(secondId.isBlank());
        assertNotEquals(firstId, secondId);
    }
}
