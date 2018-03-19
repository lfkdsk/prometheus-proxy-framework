import lexer.Lexer;
import org.junit.jupiter.api.Test;
import token.ItemType;
import token.TokenItem;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static token.TokenItem.of;

class LexerTest {
    static class TestItem {
        String input;
        TokenItem[] expected;
        boolean fail;
        boolean seriesDesc;

        private TestItem(String input, boolean fail, boolean seriesDesc, TokenItem... expected) {
            this.input = input;
            this.expected = expected;
            this.fail = fail;
            this.seriesDesc = seriesDesc;
        }

        public static TestItem of(String input, boolean fail, boolean seriesDesc, TokenItem... expected) {
            return new TestItem(input, fail, seriesDesc, expected);
        }

        public static TestItem of(String input, TokenItem... expected) {
            return new TestItem(input, true, false, expected);
        }

        public void test() {
            testLexer(this);
        }
    }

    // Test common stuff.
    @Test
    void testComma() {
        TestItem.of(
                ",",
                TokenItem.of(ItemType.itemComma, 0, ",")
        ).test();
    }

    @Test
    void testSpace() {
        TestItem.of(
                " \r \n \t "
                // empty expected array
        ).test();
    }

    @Test
    void testEmptyParen() {
        TestItem.of(
                "()",
                TokenItem.of(ItemType.itemLeftParen, 0, "("),
                TokenItem.of(ItemType.itemRightParen, 1, ")")
        ).test();
    }

    @Test
    void testEmptyBrace() {
        TestItem.of(
                "{}",
                TokenItem.of(ItemType.itemLeftBrace, 0, "{"),
                TokenItem.of(ItemType.itemRightBrace, 1, "}")
        ).test();
    }

    @Test
    void testSimpleBracket() {
        TestItem.of(
                "[5m]",
                TokenItem.of(ItemType.itemLeftBracket, 0, "["),
                TokenItem.of(ItemType.itemDuration, 1, "5m"),
                TokenItem.of(ItemType.itemRightBracket, 3, "]")
        ).test();
    }

    // test number
    @Test
    void testSimpleNumber() {
        TestItem.of(
                "1",
                TokenItem.of(ItemType.itemNumber, 0, "1")
        ).test();
    }

    @Test
    void testSimpleNumber1() {
        TestItem.of(
                "4.23",
                TokenItem.of(ItemType.itemNumber, 0, "4.23")
        ).test();
    }

    @Test
    void testSimpleNumber2() {
        TestItem.of(
                ".3",
                TokenItem.of(ItemType.itemNumber, 0, ".3")
        ).test();
    }

    @Test
    void testSimpleNumber3() {
        TestItem.of(
                "5.",
                TokenItem.of(ItemType.itemNumber, 0, "5.")
        ).test();
    }

    @Test
    void testNaNNumber() {
        TestItem.of(
                "NaN",
                TokenItem.of(ItemType.itemNumber, 0, "NaN")
        ).test();

        TestItem.of(
                "naN",
                TokenItem.of(ItemType.itemNumber, 0, "naN")
        ).test();
    }

    @Test
    void testInfNumber() {
        TestItem.of(
                "Inf",
                TokenItem.of(ItemType.itemNumber, 0, "Inf")
        ).test();

        TestItem.of(
                "inF",
                TokenItem.of(ItemType.itemNumber, 0, "inF")
        ).test();

        TestItem.of(
                "+inF",
                TokenItem.of(ItemType.itemADD, 0, "+"),
                TokenItem.of(ItemType.itemNumber, 1, "inF")
        ).test();

        TestItem.of(
                "+inF 123",
                TokenItem.of(ItemType.itemADD, 0, "+"),
                TokenItem.of(ItemType.itemNumber, 1, "inF"),
                TokenItem.of(ItemType.itemNumber, 5, "123")
        ).test();

        TestItem.of(
                "Infoo",
                TokenItem.of(ItemType.itemIdentifier, 0, "Infoo")
        ).test();
    }

    @Test
    void testMultiNumber() {
        TestItem.of(
                "Nan 123",
                TokenItem.of(ItemType.itemNumber, 0, "Nan"),
                TokenItem.of(ItemType.itemNumber, 4, "123")
        ).test();

        TestItem.of(
                "0x123",
                TokenItem.of(ItemType.itemNumber, 0, "0x123")
        ).test();
    }

    @Test
    void testIdentifier() {
        TestItem.of(
                "NaN123",
                TokenItem.of(ItemType.itemIdentifier, 0, "NaN123")
        ).test();
    }

    @Test
    void testStrings() {
        TestItem.of(
                "\"test\\tsequence\"",
                TokenItem.of(ItemType.itemString, 0, "\"test\\tsequence\"")
        ).test();
    }

    @Test
    void testMultiStrings() {
        TestItem.of(
                "\"test\\\\.expression\"",
                TokenItem.of(ItemType.itemString, 0, "\"test\\\\.expression\"")
        ).test();

        TestItem.of(
                "\"test\\.expression\"",
                TokenItem.of(ItemType.itemError, 0, "unknown escape sequence '.'"),
                TokenItem.of(ItemType.itemString, 0, "\"test\\.expression\"")
        ).test();
    }

    @Test
    void testRawString() {
        TestItem.of(
                "`test\\.expression`",
                TokenItem.of(ItemType.itemString, 0, "`test\\.expression`")
        ).test();
    }

    static void testLexer(TestItem testItem) {
        Lexer lexer = new Lexer(testItem.input);
        lexer.run();

        // EOF symbol
        assertEquals(testItem.expected.length + 1, lexer.getItems().size());

        for (int i = 0; i < testItem.expected.length; i++) {
            TokenItem item = lexer.getItems().get(i);
            TokenItem expect = testItem.expected[i];
            assertEquals(expect, item);
        }
    }
}