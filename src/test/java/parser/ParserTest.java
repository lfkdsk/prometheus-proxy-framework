package parser;

import exception.ParserException;
import lexer.token.ItemType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.ast.Expr;
import parser.ast.expr.BinaryExpr;
import parser.ast.literal.NumberLiteral;

import java.util.Objects;

import static java.lang.String.format;
import static lexer.token.ItemType.itemADD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static parser.Parser.parser;

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

        // TODO type check

        // compare expr
        assertNotNull(expr);
        assertEquals(test.expr, expr);
    }
}