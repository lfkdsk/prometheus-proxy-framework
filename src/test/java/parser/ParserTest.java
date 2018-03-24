package parser;

import exception.ParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import parser.ast.Expr;
import parser.ast.expr.BinaryExpr;
import parser.ast.expr.ParenExpr;
import parser.ast.expr.UnaryExpr;
import parser.ast.literal.NumberLiteral;
import parser.ast.literal.StringLiteral;
import parser.ast.value.AggregateExpr;
import parser.ast.value.MatrixSelector;
import parser.ast.value.VectorMatching;
import parser.ast.value.VectorSelector;
import parser.ast.match.Call;
import parser.ast.match.Matcher;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static java.lang.String.format;
import static lexer.token.ItemType.*;
import static lexer.token.ItemType.itemMUL;
import static lexer.token.ItemType.itemSUB;
import static org.junit.jupiter.api.Assertions.*;
import static parser.Parser.parser;
import static parser.ast.value.VectorMatching.VectorMatchCardinality.*;
import static parser.ast.match.Functions.getFunction;
import static parser.ast.match.Labels.MetricNameLabel;

class ParserTest {
    static int testCount = 0;

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
            testCount++;
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
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "a", "bc"),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo:bar")
                )
        ).test();

        TestItem.of(
                "foo{NaN='bc'}",
                VectorSelector.of(
                        "foo",
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "NaN", "bc"),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                )
        ).test();

        TestItem.of(
                "foo{a=\"b\", foo!=\"bar\", test=~\"test\", bar!~\"baz\"}",
                VectorSelector.of(
                        "foo",
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "a", "b"),
                        mockLabelMatcher(Matcher.MatchType.MatchNotEqual, "foo", "bar"),
                        mockLabelMatcher(Matcher.MatchType.MatchRegexp, "test", "test"),
                        mockLabelMatcher(Matcher.MatchType.MatchNotRegexp, "bar", "baz"),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "foo")
                )
        ).test();
    }

    @Test
    void testSymbolErrorMsg() {
        TestItem.of(
                "{",
                true,
                "unexpected end of input inside braces"
        ).test();

        TestItem.of(
                "}",
                true,
                "unexpected character: '}'"
        ).test();

        TestItem.of(
                "some{",
                true,
                "unexpected end of input inside braces"
        ).test();

        TestItem.of(
                "some}",
                true,
                "could not parse remaining input \"}\"..."
        ).test();

        TestItem.of(
                "some_metric{a=b}",
                true,
                "unexpected identifier \"b\" in label matching, expected string"
        ).test();

        TestItem.of(
                "some_metric{a:b=\"b\"}",
                true,
                "unexpected character inside braces: ':'"
        ).test();

        TestItem.of(
                "foo{a*\"b\"}",
                true,
                "unexpected character inside braces: '*'"
        ).test();

        TestItem.of(
                "foo{a>=\"b\"}",
                true,
                // TODO(fabxc): willingly lexing wrong tokens allows for more precrise error
                // messages from the parser - consider if this is an option.
                "unexpected character inside braces: '>'"
        ).test();
    }

    @Test
    void testExpectSymbol() {
        //        TestItem.of(
        //                "some_metric{a=\"\xff\"}",
        //                true,
        //                ""
        //        ).test();

        TestItem.of(
                "foo{gibberish}",
                true,
                "expected label matching operator but got }"
        ).test();

        TestItem.of(
                "foo{1}",
                true,
                "unexpected character inside braces: '1'"
        ).test();

        TestItem.of(
                "{}",
                true,
                "vector selector must contain label matchers or metric name"
        ).test();

        TestItem.of(
                "{x=\"\"}",
                true,
                "vector selector must contain at least one non-empty matcher"
        ).test();


        TestItem.of(
                "{x=~\".*\"}",
                true,
                "vector selector must contain at least one non-empty matcher"
        ).test();

        TestItem.of(
                "{x!~\".+\"}",
                true,
                "vector selector must contain at least one non-empty matcher"
        ).test();

        TestItem.of(
                "{x!=\"a\"}",
                true,
                "vector selector must contain at least one non-empty matcher"
        ).test();

        TestItem.of(
                "foo{__name__=\"bar\"}",
                true,
                "metric name must not be set twice: \"foo\" or \"bar\""
        ).test();
    }

    // Test matrix selector.
    @Test
    void testMatrixSelector() {
        TestItem.of(
                "test[5s]",
                MatrixSelector.of(
                        "test",
                        Duration.ofSeconds(5),
                        Duration.ZERO,
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "test")
                )
        ).test();


        TestItem.of(
                "test[5m]",
                MatrixSelector.of(
                        "test",
                        Duration.ofMinutes(5),
                        Duration.ZERO,
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "test")
                )
        ).test();

        TestItem.of(
                "test[5h] OFFSET 5m",
                MatrixSelector.of(
                        "test",
                        Duration.ofHours(5),
                        Duration.ofMinutes(5),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "test")
                )
        ).test();

        TestItem.of(
                "test[5d] OFFSET 10s",
                MatrixSelector.of(
                        "test",
                        Duration.ofDays(5),
                        Duration.ofSeconds(10),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "test")
                )
        ).test();

        TestItem.of(
                "test[5w] offset 2w",
                MatrixSelector.of(
                        "test",
                        Duration.ofDays(7 * 5),
                        Duration.ofDays(7 * 2),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "test")
                )
        ).test();

        TestItem.of(
                "test{a=\"b\"}[5y] OFFSET 3d",
                MatrixSelector.of(
                        "test",
                        Duration.ofDays(5 * 365),
                        Duration.ofDays(3),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, "a", "b"),
                        mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "test")
                )
        ).test();
    }

    @Test
    void testErrorMsgInMetrix() {
        TestItem.of(
                "foo[5mm]",
                true,
                "bad duration syntax: \"5mm\""
        ).test();

        TestItem.of(
                "foo[0m]",
                true,
                "duration must be greater than 0"
        ).test();

        TestItem.of(
                "foo[5m30s]",
                true,
                "bad duration syntax: \"5m3\""
        ).test();

        TestItem.of(
                "foo[5m] OFFSET 1h30m",
                true,
                "bad number or duration syntax: \"1h3\""
        ).test();

        TestItem.of(
                "foo[\"5m\"]",
                true,
                "missing unit character in duration"
        ).test();

        TestItem.of(
                "foo[]",
                true,
                "missing unit character in duration"
        ).test();

        TestItem.of(
                "foo[1]",
                true,
                "missing unit character in duration"
        ).test();

        TestItem.of(
                "some_metric[5m] OFFSET 1",
                true,
                "unexpected number \"1\" in offset, expected duration"
        ).test();

        TestItem.of(
                "some_metric[5m] OFFSET 1mm",
                true,
                "bad number or duration syntax: \"1mm\""
        ).test();

        TestItem.of(
                "some_metric[5m] OFFSET",
                true,
                "unexpected end of input in offset, expected duration"
        ).test();

        TestItem.of(
                "some_metric OFFSET 1m[5m]",
                true,
                "could not parse remaining input \"[5m]\"..."
        ).test();

        TestItem.of(
                "(foo + bar)[5m]",
                true,
                "could not parse remaining input \"[5m]\"..."
        ).test();
    }

    // Test aggregation.
    @Test
    void testAggregation() {
        TestItem.of(
                "sum by (foo)(some_metric)",
                AggregateExpr.of(
                        itemSum,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        Arrays.asList("foo")
                )
        ).test();

        TestItem.of(
                "avg by (foo)(some_metric)",
                AggregateExpr.of(
                        itemAvg,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        Arrays.asList("foo")
                )
        ).test();

        TestItem.of(
                "max by (foo)(some_metric)",
                AggregateExpr.of(
                        itemMax,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        Arrays.asList("foo")
                )
        ).test();

        TestItem.of(
                "sum without (foo) (some_metric)",
                AggregateExpr.of(
                        itemSum,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        null,
                        Arrays.asList("foo"),
                        true
                )
        ).test();

        TestItem.of(
                "sum (some_metric) without (foo)",
                AggregateExpr.of(
                        itemSum,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        null,
                        Arrays.asList("foo"),
                        true
                )
        ).test();

        TestItem.of(
                "stddev(some_metric)",
                AggregateExpr.of(
                        itemStddev,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        )
                )
        ).test();

        TestItem.of(
                "stdvar by (foo)(some_metric)",
                AggregateExpr.of(
                        itemStdvar,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        Arrays.asList("foo")
                )
        ).test();

        TestItem.of(
                "sum by ()(some_metric)",
                AggregateExpr.of(
                        itemSum,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        Collections.emptyList()
                )
        ).test();

        TestItem.of(
                "topk(5, some_metric)",
                AggregateExpr.of(
                        itemTopK,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        NumberLiteral.of(5)
                )
        ).test();

        TestItem.of(
                "count_values(\"value\", some_metric)",
                AggregateExpr.of(
                        itemCountValues,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        StringLiteral.of("value")
                )
        ).test();
    }

    // Test usage of keywords as label names.
    @Test
    void testUsageOfKeyWordsAsLabelNames() {
        TestItem.of(
                "sum without(and, by, avg, count, alert, annotations)(some_metric)",
                AggregateExpr.of(
                        itemSum,
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        null,
                        Arrays.asList("and", "by", "avg", "count", "alert", "annotations"),
                        true
                )
        ).test();
    }

    @Test
    void testErrorMsgInAggs() {
        TestItem.of(
                "sum without(==)(some_metric)",
                true,
                "unexpected Item<itemEQL,12,==> in grouping opts, expected label"
        ).test();

        TestItem.of(
                "sum some_metric by (test)",
                true,
                "unexpected identifier \"some_metric\" in aggregation, expected ("
        ).test();

        TestItem.of(
                "sum (some_metric) by test",
                true,
                "unexpected identifier \"test\" in grouping opts, expected ("
        ).test();


        TestItem.of(
                "sum () by (test)",
                true,
                "no valid expression found"
        ).test();


        TestItem.of(
                "MIN keep_common (some_metric)",
                true,
                "unexpected identifier \"keep_common\" in aggregation, expected ("
        ).test();

        TestItem.of(
                "MIN (some_metric) keep_common",
                true,
                "could not parse remaining input \"keep_common\"..."
        ).test();

        TestItem.of(
                "sum (some_metric) without (test) by (test)",
                true,
                "could not parse remaining input \"by (test)\"..."
        ).test();

        TestItem.of(
                "topk(some_metric)",
                true,
                "unexpected Item<itemRightParen,16,)> in aggregation, expected ,"
        ).test();
    }

    @Test
    void testTypeCheck() {
        TestItem.of(
                "topk(some_metric, other_metric)",
                true,
                "expected type scalar in aggregation parameter, got instant vector"
        ).test();

        TestItem.of(
                "count_values(5, other_metric)",
                true,
                "expected type string in aggregation parameter, got scalar"
        ).test();
    }

    // Test function calls.
    @Test
    void testFunction() {
        TestItem.of(
                "time()",
                Call.of(
                        getFunction("time"),
                        Collections.emptyList()
                )
        ).test();
    }

    @Test
    void testMultiFunctions() {
        TestItem.of(
                "floor(some_metric{foo!=\"bar\"})",
                Call.of(
                        getFunction("floor"),
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchNotEqual, "foo", "bar"),
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        )
                )
        ).test();

        TestItem.of(
                "floor(some_metric{foo!=\"bar\"})",
                Call.of(
                        getFunction("floor"),
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchNotEqual, "foo", "bar"),
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        )
                )
        ).test();

        TestItem.of(
                "rate(some_metric[5m])",
                Call.of(
                        getFunction("rate"),
                        MatrixSelector.of(
                                "some_metric",
                                Duration.ofMinutes(5),
                                Duration.ZERO,
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        )
                )
        ).test();

        TestItem.of(
                "round(some_metric)",
                Call.of(
                        getFunction("round"),
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        )
                )
        ).test();

        TestItem.of(
                "round(some_metric, 5)",
                Call.of(
                        getFunction("round"),
                        VectorSelector.of(
                                "some_metric",
                                mockLabelMatcher(Matcher.MatchType.MatchEqual, MetricNameLabel, "some_metric")
                        ),
                        NumberLiteral.of(5)
                )
        ).test();
    }

    @Test
    void testErrorMsgInFunctions() {
        TestItem.of(
                "floor()",
                true,
                "expected 1 argument(s) in call to \"floor\", got 0"
        ).test();

        TestItem.of(
                "floor(some_metric, other_metric)",
                true,
                "expected 1 argument(s) in call to \"floor\", got 2"
        ).test();

        TestItem.of(
                "floor(1)",
                true,
                "expected type instant vector in call to function \"floor\", got scalar"
        ).test();

        TestItem.of(
                "non_existent_function_far_bar()",
                true,
                "unknown function with name \"non_existent_function_far_bar\""
        ).test();

        TestItem.of(
                "rate(some_metric)",
                true,
                "expected type range vector in call to function \"rate\", got instant vector"
        ).test();

        //"label_replace(a, `b`, `c\xff`, `d`, `.*`)"
    }

    // Fuzzing regression tests.
    @Test
    void testFuzzingRegression() {
        TestItem.of(
                "-=",
                true,
                "no valid expression found"
        ).test();

        TestItem.of(
                "++-++-+-+-<",
                true,
                "no valid expression found"
        ).test();

        TestItem.of(
                "e-+=/(0)",
                true,
                "no valid expression found"
        ).test();

        TestItem.of(
                "-If",
                true,
                "no valid expression found"
        ).test();
    }

    // String quoting and escape sequence interpretation tests.
    @Test
    void testStringQuoting() {
        TestItem.of(
                "\"double-quoted string \\\" with escaped quote\"",
                StringLiteral.of(
                        "double-quoted string \\\" with escaped quote"
                )
        ).test();

        TestItem.of(
                "'single-quoted string \\' with escaped quote'",
                StringLiteral.of(
                        "single-quoted string \' with escaped quote"
                )
        ).test();

        TestItem.of(
                "`backtick-quoted string`",
                StringLiteral.of(
                        "backtick-quoted string"
                )
        ).test();

        TestItem.of(
                "\"\\a\\b\\f\\n\\r\\t\\v\\\\\\\" - \\xFF\\377\\u1234\\U00010111\\U0001011111☺\"",
                StringLiteral.of(
                        "\\a\\b\\f\\n\\r\\t\\v\\\\\\\" - \\xFF\\377\\u1234\\U00010111\\U0001011111☺"
                )
        ).test();

        TestItem.of(
                "'\\a\\b\\f\\n\\r\\t\\v\\\\\\\' - \\xFF\\377\\u1234\\U00010111\\U0001011111☺'",
                StringLiteral.of(
                        "\\a\\b\\f\\n\\r\\t\\v\\\\\' - \\xFF\\377\\u1234\\U00010111\\U0001011111☺"
                )
        ).test();

        TestItem.of(
                "`\\a\\b\\f\\n\\r\\t\\v\\\\\\\"\\' - \\xFF\\377\\u1234\\U00010111\\U0001011111☺`",
                StringLiteral.of(
                        "\\a\\b\\f\\n\\r\\t\\v\\\\\\\"\\' - \\xFF\\377\\u1234\\U00010111\\U0001011111☺"
                )
        ).test();

        TestItem.of(
                "`\\\\``",
                true,
                "could not parse remaining input \"`\"..."
        ).test();

        TestItem.of(
                "\"\\",
                true,
                "escape sequence not terminated"
        ).test();

        TestItem.of(
                "\"\\c\"",
                true,
                "unknown escape sequence 'c'"
        ).test();

        TestItem.of(
                "\"\\x.\"",
                true,
                "illegal character '.' in escape sequence"
        ).test();
    }

    // NaN has no equality. Thus, we need a separate test for it.
    @Test
    void testNaNExpression() {
        Parser parser = parser("NaN");
        Expr expr = parser.parserExpr();
        assertTrue(expr instanceof NumberLiteral);
    }

    @AfterAll
    static void afterAll() {
        System.out.println();
        System.out.println(format("account to %s tests", testCount));
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
                throw new RuntimeException(format("expected error to contain \"%s\" but got \"%s\"", test.errorMsg, excepted.getMessage()), excepted);
            }
            assertEquals(test.errorMsg, ((ParserException) excepted).getErrorMsg());
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
                if (!test.errorMsg.equals(((ParserException) excepted).getErrorMsg())) {
                    System.err.printf("unexpected error on input '%s'", test.input);
                    throw new RuntimeException(format("expected error to contain %s but got %s", test.errorMsg, excepted.getMessage()), excepted);
                }
                assertEquals(test.errorMsg, ((ParserException) excepted).getErrorMsg());
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