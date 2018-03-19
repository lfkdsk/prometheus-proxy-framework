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

    @Test
    void testSimpleNumber() {
        TestItem.of(
                "1",
                TokenItem.of(ItemType.itemNumber, 0, "1")
        );
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