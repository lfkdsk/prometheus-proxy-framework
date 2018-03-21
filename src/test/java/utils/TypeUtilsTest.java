package utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static utils.TypeUtils.count;

class TypeUtilsTest {

    @Test
    void testCount() {
        assertEquals(5, count("  \n n \n \n \n \n", '\n'));
    }

    @Test
    void testTypeStr() {
        String ctx = "5y";
        assertEquals(Duration.ofDays(5 * 365), TypeUtils.parseDuration(ctx));
    }
}