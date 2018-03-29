package io.dashbase.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringsTest {
    @Test
    void testStrings() {
//        System.out.println(Strings.unquote("\"\\a\\b\\f\\n\\r\\t\\v\\\\\\\" - \\xFF\\377\\u1234\\U00010111\\U0001011111☺\""));
        assertEquals(
                "\\a\\b\\f\\n\\r\\t\\v\\\\\\\" - \\xFF\\377\\u1234\\U00010111\\U0001011111☺",
                Strings.unquote("\"\\a\\b\\f\\n\\r\\t\\v\\\\\\\" - \\xFF\\377\\u1234\\U00010111\\U0001011111☺\""));
    }
}