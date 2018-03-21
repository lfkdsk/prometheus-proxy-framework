package parser;

import exception.ParserException;
import lexer.token.ItemType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.ast.Expr;
import parser.ast.expr.BinaryExpr;
import parser.ast.expr.ParenExpr;
import parser.ast.expr.UnaryExpr;
import parser.ast.literal.NumberLiteral;
import parser.ast.value.VectorSelector;
import parser.match.Labels;
import parser.match.Matcher;

import java.util.Objects;

import static java.lang.String.format;
import static lexer.token.ItemType.*;
import static lexer.token.ItemType.itemMUL;
import static lexer.token.ItemType.itemSUB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static parser.Parser.parser;
import static parser.match.Labels.MetricName;
import static parser.match.Labels.MetricNameLabel;

class ParserTest {
    static final class TestItem {
        final String input;
        final boolean fail;
        final String errorMsg;
        final Expr expr;


        private TestItem(String input, boolean fail, String errorMsg, Expr expr) {
            this.input = input;
            this.fail = fail;
            this.errorMsg = errorMsg;
            this.expr = expr;
        }

        public static TestItem of(String input, boolean fail, String errorMsg, Expr expr) {
            return new TestItem(input, fail, errorMsg, expr);
        }

        public static TestItem of(String input, boolean fail, String errorMsg) {
            return new TestItem(input, fail, errorMsg, null);
        }

        public static TestItem of(String input, Expr expr) {
            return new TestItem(input, false, null, expr);
        }

        public void test() {
            testParser(this);
        }
    }

    @Test
    void testNumberLiteral() {
        TestItem.of(
                "1",
                NumberLiteral.of(1)
        ).test();
    }

    @Test
    void testInfNumber() {
        TestItem.of(
                "+Inf",
                NumberLiteral.of(Double.POSITIVE_INFINITY)
        ).test();

        TestItem.of(
                "-Inf",
                NumberLiteral.of(Double.NEGATIVE_INFINITY)
        ).test();
    }

    @Test
    void testPureNumber() {
        TestItem.of(
                ".5",
                NumberLiteral.of(0.5)
        ).test();

        TestItem.of(
                "5.",
                NumberLiteral.of(5)
        ).test();

        TestItem.of(
                "123.4567",
                NumberLiteral.of(123.4567)
        ).test();
    }

    @Test
    void testComplexNumber() {
        TestItem.of(
                "5e-3",
                NumberLiteral.of(0.005)
        ).test();

        TestItem.of(
                "5e3",
                NumberLiteral.of(5000)
        ).test();

        TestItem.of(
                "0xc",
                NumberLiteral.of(12)
        ).test();

        TestItem.of(
                "0755",
                NumberLiteral.of(493)
        ).test();

        TestItem.of(
                "+5.5e-3",
                NumberLiteral.of(0.0055)
        ).test();

        TestItem.of(
                "-0755",
                NumberLiteral.of(-493)
        ).test();
    }

    @Test
    void testBinaryExpr() {
        TestItem.of(
                "1 + 1",
                BinaryExpr.of(
                        itemADD,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1)
                )
        ).test();
        TestItem.of(
                "1 - 1",
                BinaryExpr.of(
                        itemSUB,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1)
                )
        ).test();
        TestItem.of(
                "1 * 1",
                BinaryExpr.of(
                        itemMUL,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1)
                )
        ).test();

        TestItem.of(
                "1 % 1",
                BinaryExpr.of(
                        itemMOD,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1)
                )
        ).test();

        TestItem.of(
                "1/1",
                BinaryExpr.of(
                        itemDIV,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1)
                )
        ).test();
    }

    @Test
    void testBoolBinaryExpr() {
        TestItem.of(
                "1 == bool 1",
                BinaryExpr.of(
                        itemEQL,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1),
                        true
                )
        ).test();

        TestItem.of(
                "1 != bool 1",
                BinaryExpr.of(
                        itemNEQ,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1),
                        true
                )
        ).test();

        TestItem.of(
                "1 > bool 1",
                BinaryExpr.of(
                        itemGTR,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1),
                        true
                )
        ).test();

        TestItem.of(
                "1 >= bool 1",
                BinaryExpr.of(
                        itemGTE,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1),
                        true
                )
        ).test();

        TestItem.of(
                "1 < bool 1",
                BinaryExpr.of(
                        itemLSS,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1),
                        true
                )
        ).test();

        TestItem.of(
                "1 <= bool 1",
                BinaryExpr.of(
                        itemLTE,
                        NumberLiteral.of(1),
                        NumberLiteral.of(1),
                        true
                )
        ).test();
    }

    @Test
    void testComplexBinaryExpr() {
        TestItem.of(
                "+1 + -2 * 1",
                BinaryExpr.of(
                        itemADD,
                        NumberLiteral.of(1),
                        BinaryExpr.of(
                                itemMUL,
                                NumberLiteral.of(-2),
                                NumberLiteral.of(1)
                        )
                )
        ).test();

        TestItem.of(
                "1 + 2/(3*1)",
                BinaryExpr.of(
                        itemADD,
                        NumberLiteral.of(1),
                        BinaryExpr.of(
                                itemDIV,
                                NumberLiteral.of(2),
                                ParenExpr.of(
                                        BinaryExpr.of(
                                                itemMUL,
                                                NumberLiteral.of(3),
                                                NumberLiteral.of(1)
                                        )
                                )
                        )
                )
        ).test();


        TestItem.of(
                "1 < bool 2 - 1 * 2",
                BinaryExpr.of(
                        itemLSS,
                        NumberLiteral.of(1),
                        BinaryExpr.of(
                                itemSUB,
                                NumberLiteral.of(2),
                                BinaryExpr.of(
                                        itemMUL,
                                        NumberLiteral.of(1),
                                        NumberLiteral.of(2)
                                )
                        ),
                        true
                )
        ).test();
    }

    @Test
    void testSimpleMetrics() {
        TestItem.of(
                "-some_metric",
                UnaryExpr.of(
                        itemSUB,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(
                                        Matcher.MatchType.MatchEqual,
                                        MetricNameLabel,
                                        "some_metric"
                                )
                        )
                )
        ).test();

        TestItem.of(
                "+some_metric",
                UnaryExpr.of(
                        itemADD,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(
                                        Matcher.MatchType.MatchEqual,
                                        MetricNameLabel,
                                        "some_metric"
                                )
                        )
                )
        ).test();
    }

    @Test
    void testErrorMsg() {
        TestItem.of(
                "",
                true,
                "no expression found in input"
        ).test();

        TestItem.of(
                "# just a comment\\n\\n",
                true,
                "no expression found in input"
        ).test();

        TestItem.of(
                "1+",
                true,
                "no valid expression found"
        ).test();
    }

    @Test
    void testErrorMsg1() {
        TestItem.of(
                ".",
                true,
                "unexpected character: '.'"
        ).test();

        TestItem.of(
                "2.5.",
                true,
                "could not parse remaining input \".\"..."
        ).test();


        TestItem.of(
                "100..4",
                true,
                "could not parse remaining input \".4\"..."
        ).test();

        TestItem.of(
                "0deadbeef",
                true,
                "bad number or duration syntax: \"0de\""
        ).test();
    }

    @Test
    void testMultiErrors() {
        TestItem.of(
                "1 /",
                true,
                "no valid expression found"
        ).test();

        TestItem.of(
                "*1",
                true,
                "no valid expression found"
        ).test();

        TestItem.of(
                "(1))",
                true,
                "could not parse remaining input \")\"..."
        ).test();

        TestItem.of(
                "((1)",
                true,
                "unclosed left parenthesis"
        ).test();

        TestItem.of(
                "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
                // in promql error msg is "out of range"
                NumberLiteral.of(Double.POSITIVE_INFINITY)
        ).test();
    }

    @Test
    void testErrorMsg2() {
        TestItem.of(
                "(",
                true,
                "unclosed left parenthesis"
        ).test();

        TestItem.of(
                "1 and 1",
                true,
                "set operator \"and\" not allowed in binary scalar expression"
        ).test();
    }

    static void testParser(TestItem test) {
        Parser parser = parser(test.input);

        Exception excepted = null;
        Expr expr = null;
        try {
            expr = parser.parserExpr();
        } catch (Exception e) {
            excepted = e;
        }

        if (Objects.nonNull(excepted) && !(excepted instanceof ParserException)) {
            throw new RuntimeException("unexpected error occurred", excepted);
        }

        ParserException exceptedError = (ParserException) excepted;

        if (!test.fail && Objects.nonNull(exceptedError)) {
            System.err.printf("error in input '%s'", test.input);
            throw new RuntimeException(format("could not parse: %s", exceptedError.getMessage()), exceptedError);
        }

        if (test.fail && excepted != null) {
            if (!test.errorMsg.equals(((ParserException) excepted).getErrorMsg())) {
                System.err.printf("unexpected error on input '%s'", test.input);
                throw new RuntimeException(format("expected error to contain %s but got %s", test.errorMsg, excepted.getMessage()));
            }
            return;
        }
        // TODO type check

        // compare expr
        assertNotNull(expr);
        assertEquals(test.expr, expr);
    }

    static Matcher mockLabelMatcher(Matcher.MatchType type, String name, String value) {
        return Matcher.newMatcher(
                type,
                name,
                value
        );
    }
}