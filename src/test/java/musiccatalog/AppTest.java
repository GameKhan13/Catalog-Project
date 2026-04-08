package musiccatalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    @DisplayName("UT-07-TB: App.extractToken() extracts token from Bearer header")
    void extractToken_withBearerHeader_returnsTokenOnly() throws Exception {
        Method extractTokenMethod = App.class.getDeclaredMethod("extractToken", String.class);
        extractTokenMethod.setAccessible(true);

        String token = (String) extractTokenMethod.invoke(null, "Bearer abc123");

        assertEquals("abc123", token);
    }
}