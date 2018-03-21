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
import parser.ast.value.MatrixSelector;
import parser.ast.value.VectorMatching;
import parser.ast.value.VectorSelector;
import parser.match.Labels;
import parser.match.Matcher;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static java.lang.String.format;
import static lexer.token.ItemType.*;
import static lexer.token.ItemType.itemMUL;
import static lexer.token.ItemType.itemSUB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static parser.Parser.parser;
import static parser.ast.value.VectorMatching.VectorMatchCardinality.*;
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

        TestItem.of(
                "1 == 1",
                true,
                "comparisons between scalars must use BOOL modifier"
        ).test();

        TestItem.of(
                "1 or 1",
                true,
                "set operator \"or\" not allowed in binary scalar expression"
        ).test();


        TestItem.of(
                "1 unless 1",
                true,
                "set operator \"unless\" not allowed in binary scalar expression"
        ).test();
    }

    @Test
    void testErrorInRemain() {
        TestItem.of(
                "1 !~ 1",
                true,
                "could not parse remaining input \"!~ 1\"..."
        ).test();

        TestItem.of(
                "1 =~ 1",
                true,
                "could not parse remaining input \"=~ 1\"..."
        ).test();
    }

    @Test
    void testInWrongType() {
        TestItem.of(
                "-\"string\"",
                true,
                "unary expression only allowed on expressions of type scalar or instant vector, got \"string\""
        ).test();

        TestItem.of(
                "-test[5m]",
                true,
                "unary expression only allowed on expressions of type scalar or instant vector, got \"range vector\""
        ).test();

        TestItem.of(
                "*test",
                true,
                "no valid expression found"
        ).test();

        TestItem.of(
                "1 offset 1d",
                true,
                "offset modifier must be preceded by an instant or range selector, but follows a \"NumberLiteral<1.0>\" instead"
        ).test();


        TestItem.of(
                "a - on(b) ignoring(c) d",
                true,
                "no valid expression found"
        ).test();
    }

    // Vector binary operations
    @Test
    void testVectorBinaryOp() {
        TestItem.of(
                "foo * bar",
                BinaryExpr.of(
                        itemMUL,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(CardOneToOne)
                )
        ).test();

        TestItem.of(
                "foo == 1",
                BinaryExpr.of(
                        itemEQL,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        NumberLiteral.of(1)
                )
        ).test();

        TestItem.of(
                "foo == bool 1",
                BinaryExpr.of(
                        itemEQL,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        NumberLiteral.of(1),
                        true
                )
        ).test();

        TestItem.of(
                "2.5 / bar",
                BinaryExpr.of(
                        itemDIV,
                        NumberLiteral.of(2.5),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        )
                )
        ).test();

        TestItem.of(
                "foo and bar",
                BinaryExpr.of(
                        itemLAND,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(CardManyToMany)
                )
        ).test();

        TestItem.of(
                "foo or bar",
                BinaryExpr.of(
                        itemLOR,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(CardManyToMany)
                )
        ).test();

        TestItem.of(
                "foo unless bar",
                BinaryExpr.of(
                        itemLUnless,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(CardManyToMany)
                )
        ).test();
    }

    // Test and/or precedence and reassigning of operands.
    @Test
    void testPrecedence() {
        TestItem.of(
                "foo + bar or bla and blub",
                BinaryExpr.of(
                        itemLOR,
                        BinaryExpr.of(
                                itemADD,
                                VectorSelector.of(
                                        "foo",
                                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                                ),
                                VectorSelector.of(
                                        "bar",
                                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                                ),
                                false,
                                VectorMatching.of(CardOneToOne)
                        ),
                        BinaryExpr.of(
                                itemLAND,
                                VectorSelector.of(
                                        "bla",
                                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bla")
                                ),
                                VectorSelector.of(
                                        "blub",
                                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "blub")
                                ),
                                false,
                                VectorMatching.of(CardManyToMany)
                        ),
                        false,
                        VectorMatching.of(CardManyToMany)
                )
        ).test();
    }

    // bar + on(foo) bla / on(baz, buz) group_right(test) blub
    @Test
    void testPrecedence2() {
        TestItem.of(
                "bar + on(foo) bla / on(baz, buz) group_right(test) blub",
                BinaryExpr.of(
                        itemADD,
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        BinaryExpr.of(
                                itemDIV,
                                VectorSelector.of(
                                        "bla",
                                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bla")
                                ),
                                VectorSelector.of(
                                        "blub",
                                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "blub")
                                ),
                                false,
                                VectorMatching.of(
                                        CardOneToMany,
                                        Arrays.asList("baz", "buz"),
                                        true,
                                        Collections.singletonList("test")
                                )
                        ),
                        false,
                        VectorMatching.of(
                                CardOneToOne,
                                Collections.singletonList("foo"),
                                true,
                                Collections.emptyList()
                        )

                )
        ).test();
    }

    @Test
    void testPrecedence3() {
        TestItem.of(
                "foo * on(test,blub) bar",
                BinaryExpr.of(
                        itemMUL,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardOneToOne,
                                Arrays.asList("test", "blub"),
                                true,
                                Collections.emptyList()
                        )
                )
        ).test();

        TestItem.of(
                "foo * on(test,blub) group_left bar",
                BinaryExpr.of(
                        itemMUL,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToOne,
                                Arrays.asList("test", "blub"),
                                true,
                                Collections.emptyList()
                        )
                )
        ).test();

        TestItem.of(
                "foo and on(test,blub) bar",
                BinaryExpr.of(
                        itemLAND,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToMany,
                                Arrays.asList("test", "blub"),
                                true,
                                Collections.emptyList()
                        )
                )
        ).test();


        TestItem.of(
                "foo and on() bar",
                BinaryExpr.of(
                        itemLAND,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToMany,
                                Arrays.asList(),
                                true,
                                Collections.emptyList()
                        )
                )
        ).test();

        TestItem.of(
                "foo and ignoring(test,blub) bar",
                BinaryExpr.of(
                        itemLAND,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToMany,
                                Arrays.asList("test", "blub"),
                                false,
                                Collections.emptyList()
                        )
                )
        ).test();

        TestItem.of(
                "foo and ignoring() bar",
                BinaryExpr.of(
                        itemLAND,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToMany,
                                Arrays.asList(),
                                false,
                                Collections.emptyList()
                        )
                )
        ).test();

        TestItem.of(
                "foo unless on(bar) baz",
                BinaryExpr.of(
                        itemLUnless,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "baz",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "baz")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToMany,
                                Arrays.asList("bar"),
                                true,
                                Collections.emptyList()
                        )
                )
        ).test();

        TestItem.of(
                "foo / ignoring(test,blub) group_left(blub) bar",
                BinaryExpr.of(
                        itemDIV,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToOne,
                                Arrays.asList("test", "blub"),
                                false,
                                Arrays.asList("blub")
                        )
                )
        ).test();


        TestItem.of(
                "foo / on(test,blub) group_left(bar) bar",
                BinaryExpr.of(
                        itemDIV,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToOne,
                                Arrays.asList("test", "blub"),
                                true,
                                Arrays.asList("bar")
                        )
                )
        ).test();

        TestItem.of(
                "foo / ignoring(test,blub) group_left(bar) bar",
                BinaryExpr.of(
                        itemDIV,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardManyToOne,
                                Arrays.asList("test", "blub"),
                                false,
                                Arrays.asList("bar")
                        )
                )
        ).test();
    }

    @Test
    void testPrecedence4() {
        TestItem.of(
                "foo - on(test,blub) group_right(bar,foo) bar",
                BinaryExpr.of(
                        itemSUB,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardOneToMany,
                                Arrays.asList("test", "blub"),
                                true,
                                Arrays.asList("bar", "foo")
                        )
                )
        ).test();

        TestItem.of(
                "foo - ignoring(test,blub) group_right(bar,foo) bar",
                BinaryExpr.of(
                        itemSUB,
                        VectorSelector.of(
                                "foo",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                        ),
                        VectorSelector.of(
                                "bar",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "bar")
                        ),
                        false,
                        VectorMatching.of(
                                CardOneToMany,
                                Arrays.asList("test", "blub"),
                                false,
                                Arrays.asList("bar", "foo")
                        )
                )
        ).test();
    }

    @Test
    void testErrorMsg3() {
        TestItem.of(
                "foo and 1",
                true,
                "set operator \"and\" not allowed in binary scalar expression"
        ).test();

        TestItem.of(
                "1 and foo",
                true,
                "set operator \"and\" not allowed in binary scalar expression"
        ).test();

        TestItem.of(
                "1 or foo",
                true,
                "set operator \"or\" not allowed in binary scalar expression"
        ).test();

        TestItem.of(
                "foo or 1",
                true,
                "set operator \"or\" not allowed in binary scalar expression"
        ).test();

        TestItem.of(
                "foo unless 1",
                true,
                "set operator \"unless\" not allowed in binary scalar expression"
        ).test();

        TestItem.of(
                "1 unless foo",
                true,
                "set operator \"unless\" not allowed in binary scalar expression"
        ).test();

        TestItem.of(
                "1 or on(bar) foo",
                true,
                "vector matching only allowed between instant vectors"
        ).test();

        TestItem.of(
                "foo == on(bar) 10",
                true,
                "vector matching only allowed between instant vectors"
        ).test();

        TestItem.of(
                "foo and on(bar) group_left(baz) bar",
                true,
                "no grouping allowed for \"and\" operation"
        ).test();

        TestItem.of(
                "foo and on(bar) group_right(baz) bar",
                true,
                "no grouping allowed for \"and\" operation"
        ).test();

        TestItem.of(
                "foo or on(bar) group_left(baz) bar",
                true,
                "no grouping allowed for \"or\" operation"
        ).test();

        TestItem.of(
                "foo or on(bar) group_right(baz) bar",
                true,
                "no grouping allowed for \"or\" operation"
        ).test();

        TestItem.of(
                "foo unless on(bar) group_left(baz) bar",
                true,
                "no grouping allowed for \"unless\" operation"
        ).test();

        TestItem.of(
                "foo unless on(bar) group_right(baz) bar",
                true,
                "no grouping allowed for \"unless\" operation"
        ).test();
    }

    @Test
    void testErrorMsg4() {
        TestItem.of(
                "http_requests{group=\"production\"} + on(instance) group_left(job,instance) cpu_count{type=\"smp\"}",
                true,
                "label \"instance\" must not occur in ON and GROUP clause at once"
        ).test();


        TestItem.of(
                "foo + bool bar",
                true,
                "bool modifier can only be used on comparison operators"
        ).test();

        TestItem.of(
                "foo and bool bar",
                true,
                "bool modifier can only be used on comparison operators"
        ).test();
    }

    // Test Vector selector.
    @Test
    void testVectorSelector() {
        TestItem.of(
                "foo",
                VectorSelector.of(
                        "foo",
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                )
        ).test();

        TestItem.of(
                "foo offset 5m",
                VectorSelector.of(
                        "foo",
                        Duration.ofMinutes(5),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                )
        ).test();

        TestItem.of(
                "foo:bar{a=\"bc\"}",
                VectorSelector.of(
                        "foo:bar",
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "a", "\"bc\""),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo:bar")
                )
        ).test();

        TestItem.of(
                "foo{NaN='bc'}",
                VectorSelector.of(
                        "foo",
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "NaN", "\'bc\'"),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                )
        ).test();

        TestItem.of(
                "foo{a=\"b\", foo!=\"bar\", test=~\"test\", bar!~\"baz\"}",
                VectorSelector.of(
                        "foo",
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "a", "\"b\""),
                        mockLabelMatcher(Matcher.MatchType.MatchNotEqual, "foo", "\"bar\""),
                        mockLabelMatcher(Matcher.MatchType.MatchRegexp, "test", "\"test\""),
                        mockLabelMatcher(Matcher.MatchType.MatchNotRegexp, "bar", "\"baz\""),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
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

        if (test.fail && excepted != null) {
            assertEquals(test.errorMsg, ((ParserException) excepted).getErrorMsg());
            if (!test.errorMsg.equals(((ParserException) excepted).getErrorMsg())) {
                System.err.printf("unexpected error on input '%s'", test.input);
                throw new RuntimeException(format("expected error to contain \"%s\" but got \"%s\"", test.errorMsg, excepted.getMessage()));
            }
            return;
        }

        assertNull(excepted);

        try {
            parser.typeCheck(expr);
        } catch (Exception e) {
            excepted = e;
        }

        if (Objects.nonNull(excepted) && !(excepted instanceof ParserException)) {
            throw new RuntimeException("unexpected error occurred", excepted);
        }

        if (!test.fail && Objects.nonNull(excepted)) {
            System.err.printf("error on input '%s'", test.input);
            throw new RuntimeException(format("typecheck failed: %s", excepted.getMessage()), excepted);
        }

        if (test.fail) {
            if (Objects.nonNull(excepted)) {
                assertEquals(test.errorMsg, ((ParserException) excepted).getErrorMsg());
                if (!test.errorMsg.equals(((ParserException) excepted).getErrorMsg())) {
                    System.err.printf("unexpected error on input '%s'", test.input);
                    throw new RuntimeException(format("expected error to contain %s but got %s", test.errorMsg, excepted.getMessage()), excepted);
                }
                return;
            }

            System.err.printf("error on input '%s'", test.input);
            throw new RuntimeException(format("failure expected, but passed with result: %s", expr.toString()));
        }
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