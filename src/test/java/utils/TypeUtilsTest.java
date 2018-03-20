package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static utils.TypeUtils.count;

class TypeUtilsTest {

    @Test
    void testCount() {
        assertEquals(5, count("  \n n \n \n \n \n", '\n'));
    }
}