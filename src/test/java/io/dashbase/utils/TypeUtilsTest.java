package io.dashbase.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static io.dashbase.utils.TypeUtils.count;

class TypeUtilsTest {

    @Test
    void testCount() {
        assertEquals(5, count("  \n n \n \n \n \n", '\n'));
    }

    @Test
    void testTypeStr() {
        String ctx = "5d";
        assertEquals(Duration.ofDays(5), TypeUtils.parseDuration(ctx));
    }
}