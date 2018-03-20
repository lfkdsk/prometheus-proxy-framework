import lexer.QueryLexer;
import lexer.state.States;
import org.junit.jupiter.api.Test;
import token.TokenItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static token.ItemType.*;

class LexerTest {
    static final class TestItem {
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

        static TestItem of(String input, boolean fail, boolean seriesDesc, TokenItem... expected) {
            return new TestItem(input, fail, seriesDesc, expected);
        }

        static TestItem of(String input, boolean fail, TokenItem... expected) {
            return new TestItem(input, fail, false, expected);
        }

        static TestItem of(String input, TokenItem... expected) {
            return new TestItem(input, false, false, expected);
        }

        void test() {
            testLexer(this);
        }
    }

    // Test common stuff.
    @Test
    void testComma() {
        TestItem.of(
                ",",
                TokenItem.of(itemComma, 0, ",")
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
                TokenItem.of(itemLeftParen, 0, "("),
                TokenItem.of(itemRightParen, 1, ")")
        ).test();
    }

    @Test
    void testEmptyBrace() {
        TestItem.of(
                "{}",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemRightBrace, 1, "}")
        ).test();
    }

    @Test
    void testSimpleBracket() {
        TestItem.of(
                "[5m]",
                TokenItem.of(itemLeftBracket, 0, "["),
                TokenItem.of(itemDuration, 1, "5m"),
                TokenItem.of(itemRightBracket, 3, "]")
        ).test();
    }

    // test number
    @Test
    void testSimpleNumber() {
        TestItem.of(
                "1",
                TokenItem.of(itemNumber, 0, "1")
        ).test();
    }

    @Test
    void testSimpleNumber1() {
        TestItem.of(
                "4.23",
                TokenItem.of(itemNumber, 0, "4.23")
        ).test();
    }

    @Test
    void testSimpleNumber2() {
        TestItem.of(
                ".3",
                TokenItem.of(itemNumber, 0, ".3")
        ).test();
    }

    @Test
    void testSimpleNumber3() {
        TestItem.of(
                "5.",
                TokenItem.of(itemNumber, 0, "5.")
        ).test();
    }

    @Test
    void testNaNNumber() {
        TestItem.of(
                "NaN",
                TokenItem.of(itemNumber, 0, "NaN")
        ).test();

        TestItem.of(
                "naN",
                TokenItem.of(itemNumber, 0, "naN")
        ).test();
    }

    @Test
    void testInfNumber() {
        TestItem.of(
                "Inf",
                TokenItem.of(itemNumber, 0, "Inf")
        ).test();

        TestItem.of(
                "inF",
                TokenItem.of(itemNumber, 0, "inF")
        ).test();

        TestItem.of(
                "+inF",
                TokenItem.of(itemADD, 0, "+"),
                TokenItem.of(itemNumber, 1, "inF")
        ).test();

        TestItem.of(
                "+inF 123",
                TokenItem.of(itemADD, 0, "+"),
                TokenItem.of(itemNumber, 1, "inF"),
                TokenItem.of(itemNumber, 5, "123")
        ).test();

        TestItem.of(
                "Infoo",
                TokenItem.of(itemIdentifier, 0, "Infoo")
        ).test();
    }

    @Test
    void testMultiNumber() {
        TestItem.of(
                "Nan 123",
                TokenItem.of(itemNumber, 0, "Nan"),
                TokenItem.of(itemNumber, 4, "123")
        ).test();

        TestItem.of(
                "0x123",
                TokenItem.of(itemNumber, 0, "0x123")
        ).test();
    }

    @Test
    void testIdentifier() {
        TestItem.of(
                "NaN123",
                TokenItem.of(itemIdentifier, 0, "NaN123")
        ).test();
    }

    @Test
    void testStrings() {
        TestItem.of(
                "\"test\\tsequence\"",
                TokenItem.of(itemString, 0, "\"test\\tsequence\"")
        ).test();
    }

    @Test
    void testMultiStrings() {
        TestItem.of(
                "\"test\\\\.expression\"",
                TokenItem.of(itemString, 0, "\"test\\\\.expression\"")
        ).test();

        TestItem.of(
                "\"test\\.expression\"",
                TokenItem.of(itemError, 0, "unknown escape sequence '.'"),
                TokenItem.of(itemString, 0, "\"test\\.expression\"")
        ).test();
    }

    @Test
    void testRawString() {
        TestItem.of(
                "`test\\.expression`",
                TokenItem.of(itemString, 0, "`test\\.expression`")
        ).test();
    }

    @Test
    void testNonCode() {
        TestItem.of(
                ".٩",
                true,
                false
        ).test();
    }

    // Test duration.
    @Test
    void testDurations() {
        TestItem.of(
                "5s",
                TokenItem.of(itemDuration, 0, "5s")
        ).test();

        TestItem.of(
                "123m",
                TokenItem.of(itemDuration, 0, "123m")
        ).test();

        TestItem.of(
                "1h",
                TokenItem.of(itemDuration, 0, "1h")
        ).test();

        TestItem.of(
                "3w",
                TokenItem.of(itemDuration, 0, "3w")
        ).test();

        TestItem.of(
                "1y",
                TokenItem.of(itemDuration, 0, "1y")
        ).test();
    }

    // Test identifiers.
    @Test
    void testMoreIdentifier() {
        TestItem.of(
                "abc",
                TokenItem.of(itemIdentifier, 0, "abc")
        ).test();

        TestItem.of(
                "abc d",
                TokenItem.of(itemIdentifier, 0, "abc"),
                TokenItem.of(itemIdentifier, 4, "d")
        ).test();
    }

    @Test
    void testMetricIdentifier() {
        TestItem.of(
                "a:bc",
                TokenItem.of(itemMetricIdentifier, 0, "a:bc")
        ).test();

        TestItem.of(
                ":bc",
                TokenItem.of(itemMetricIdentifier, 0, ":bc")
        ).test();

        TestItem.of(
                "0a:bc",
                true
        ).test();
    }

    // test comments
    @Test
    void testComments() {
        TestItem.of(
                "# some comment",
                TokenItem.of(itemComment, 0, "# some comment")
        ).test();

        TestItem.of(
                "5 # 1+1\n5",
                TokenItem.of(itemNumber, 0, "5"),
                TokenItem.of(itemComment, 2, "# 1+1"),
                TokenItem.of(itemNumber, 8, "5")
        ).test();
    }

    // test operators
    @Test
    void testOperators() {
        TestItem.of(
                "=",
                TokenItem.of(itemAssign, 0, "=")
        ).test();

        // Inside braces equality is a single '=' character.
        TestItem.of(
                "{=}",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemEQL, 1, "="),
                TokenItem.of(itemRightBrace, 2, "}")
        ).test();

        TestItem.of(
                "==",
                TokenItem.of(itemEQL, 0, "==")
        ).test();

        TestItem.of(
                "!=",
                TokenItem.of(itemNEQ, 0, "!=")
        ).test();


        TestItem.of(
                "<",
                TokenItem.of(itemLSS, 0, "<")
        ).test();

        TestItem.of(
                ">",
                TokenItem.of(itemGTR, 0, ">")
        ).test();

        TestItem.of(
                ">=",
                TokenItem.of(itemGTE, 0, ">=")
        ).test();

        TestItem.of(
                "<=",
                TokenItem.of(itemLTE, 0, "<=")
        ).test();


        TestItem.of(
                "+",
                TokenItem.of(itemADD, 0, "+")
        ).test();

        TestItem.of(
                "-",
                TokenItem.of(itemSUB, 0, "-")
        ).test();

        TestItem.of(
                "*",
                TokenItem.of(itemMUL, 0, "*")
        ).test();

        TestItem.of(
                "/",
                TokenItem.of(itemDIV, 0, "/")
        ).test();

        TestItem.of(
                "^",
                TokenItem.of(itemPOW, 0, "^")
        ).test();

        TestItem.of(
                "%",
                TokenItem.of(itemMOD, 0, "%")
        ).test();
    }

    // and or ....
    @Test
    void testJoinOp() {
        TestItem.of(
                "AND",
                TokenItem.of(itemLAND, 0, "AND")
        ).test();

        TestItem.of(
                "or",
                TokenItem.of(itemLOR, 0, "or")
        ).test();

        TestItem.of(
                "unless",
                TokenItem.of(itemLUnless, 0, "unless")
        ).test();
    }

    @Test
    void testAggsAndKeyWord() {
        States.keywordsMap
                .entrySet()
                .stream()
                .filter(Objects::nonNull)
                .forEach(entry ->
                        TestItem.of(
                                entry.getKey(),
                                TokenItem.of(entry.getValue(), 0, entry.getKey())
                        ).test()
                );
    }

    // Test common errors.
    @Test
    void testCommonError() {
        List<String> errors = Arrays.asList(
                "=~", "!~", "!(", "1a"
        );

        errors.forEach(error -> TestItem.of(error, true).test());
    }

    // Test mismatched parens.
    @Test
    void testMisMatchedParens() {
        List<String> errors = Arrays.asList(
                "(", "())", "(()", "{", "}", "{{", "{{}}", "[", "[[", "[]]", "[[]]", "]"
        );

        errors.forEach(error -> TestItem.of(error, true).test());
    }

    // Test encoding issues.
    @Test
    void testEncodingIssues() {
        List<String> errors = Collections.singletonList(
                "\"\\xff\"" // another test cannot write in java
        );

        errors.forEach(error -> TestItem.of(error, true).test());
    }

    // Test Selector.
    @Test
    void testErrorSelector() {
        // errors
        List<String> errors = Arrays.asList(
                "北京", "{北京='a'}", "{0a='a'}",
                "{alert!#\"bar\"}", "{foo:a=\"bar\"}"
        );

        errors.forEach(error -> TestItem.of(error, true).test());
    }

    @Test
    void testSelector() {
        TestItem.of(
                "{foo='bar'}",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemIdentifier, 1, "foo"),
                TokenItem.of(itemEQL, 4, "="),
                TokenItem.of(itemString, 5, "'bar'"),
                TokenItem.of(itemRightBrace, 10, "}")
        ).test();

        TestItem.of(
                "{foo=\"bar\"}",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemIdentifier, 1, "foo"),
                TokenItem.of(itemEQL, 4, "="),
                TokenItem.of(itemString, 5, "\"bar\""),
                TokenItem.of(itemRightBrace, 10, "}")
        ).test();

        TestItem.of(
                "{foo=\"bar\\\"bar\"}",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemIdentifier, 1, "foo"),
                TokenItem.of(itemEQL, 4, "="),
                TokenItem.of(itemString, 5, "\"bar\\\"bar\""),
                TokenItem.of(itemRightBrace, 15, "}")
        ).test();

        TestItem.of(
                "{NaN\t!= \"bar\" }",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemIdentifier, 1, "NaN"),
                TokenItem.of(itemNEQ, 5, "!="),
                TokenItem.of(itemString, 8, "\"bar\""),
                TokenItem.of(itemRightBrace, 14, "}")
        ).test();

        TestItem.of(
                "{alert=~\"bar\" }",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemIdentifier, 1, "alert"),
                TokenItem.of(itemEQLRegex, 6, "=~"),
                TokenItem.of(itemString, 8, "\"bar\""),
                TokenItem.of(itemRightBrace, 14, "}")
        ).test();

        TestItem.of(
                "{on!~\"bar\"}",
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemIdentifier, 1, "on"),
                TokenItem.of(itemNEQRegex, 3, "!~"),
                TokenItem.of(itemString, 5, "\"bar\""),
                TokenItem.of(itemRightBrace, 10, "}")
        ).test();
    }

    // Test series description.
    @Test
    void testSeriesDesc() {
        // {itemLeftBrace, 0, `{`},
        // {itemRightBrace, 1, `}`},
        // {itemBlank, 3, `_`},
        // {itemNumber, 5, `1`},
        // {itemTimes, 7, `x`},
        // {itemNumber, 9, `.3`},

        TestItem.of(
                "{} _ 1 x .3",
                false,
                true,
                TokenItem.of(itemLeftBrace, 0, "{"),
                TokenItem.of(itemRightBrace, 1, "}"),
                TokenItem.of(itemBlank, 3, "_"),
                TokenItem.of(itemNumber, 5, "1"),
                TokenItem.of(itemTimes, 7, "x"),
                TokenItem.of(itemNumber, 9, ".3")
        ).test();

        TestItem.of(
                "metric +Inf Inf NaN",
                false,
                true,
                TokenItem.of(itemIdentifier, 0, "metric"),
                TokenItem.of(itemADD, 7, "+"),
                TokenItem.of(itemNumber, 8, "Inf"),
                TokenItem.of(itemNumber, 12, "Inf"),
                TokenItem.of(itemNumber, 16, "NaN")
        ).test();

        TestItem.of(
                "metric 1+1x4",
                false,
                true,
                TokenItem.of(itemIdentifier, 0, "metric"),
                TokenItem.of(itemNumber, 7, "1"),
                TokenItem.of(itemADD, 8, "+"),
                TokenItem.of(itemNumber, 9, "1"),
                TokenItem.of(itemTimes, 10, "x"),
                TokenItem.of(itemNumber, 11, "4")
        ).test();
    }

    static void testLexer(TestItem testItem) {
        QueryLexer lexer = new QueryLexer(testItem.input);
        lexer.setSeriesDesc(testItem.seriesDesc);
        lexer.run();

        TokenItem lastItem = lexer.getItems().get(lexer.getItems().size() - 1);
        if (testItem.fail) {
            if (lastItem.type != itemError) {
                System.err.printf("input %s \n", testItem.input);
                System.err.println("expected lexing error but did not fail");
                throw new RuntimeException("exception in fail tests");
            }
            return;
        }

        if (lastItem.type == itemError) {
            System.err.printf("input %s \n", testItem.input);
            System.err.printf("unexpected lexing error at position %d: %s \n", lastItem.position, lastItem.text);
            throw new RuntimeException("exception in tests' error");
        }

        // EOF symbol
        assertEquals(testItem.expected.length + 1, lexer.getItems().size());

        for (int i = 0; i < testItem.expected.length; i++) {
            TokenItem item = lexer.getItems().get(i);
            TokenItem expect = testItem.expected[i];
            assertEquals(expect, item);
        }
    }
}