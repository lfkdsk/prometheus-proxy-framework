package io.dashbase.eval;

import io.dashbase.lexer.token.ItemType;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.parser.ast.value.VectorSelector;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import rapid.api.query.Conjunction;
import rapid.api.query.EqualityQuery;
import rapid.api.query.Query;
import rapid.api.query.StringQuery;

import java.util.Collections;

import static io.dashbase.parser.ast.match.Labels.MetricNameLabel;
import static io.dashbase.parser.ast.match.Matcher.newMatcher;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class QueryEvalVisitorTest {
    private final QueryEvalVisitor visitor = new QueryEvalVisitor();

    @Test
    void testNumberQuery() {
        Query query = visitor.visit(NumberLiteral.of(1));
        assertNotNull(query);

        assertThat(
                "Number Literal will convertTo String Query",
                query,
                instanceOf(StringQuery.class)
        );

        assertThat(
                "queryStr equal",
                ((StringQuery) query).queryStr,
                is("1")
        );
    }

    @Test
    void testStringQuery() {
        Query query = visitor.visit(StringLiteral.of("lfkdsk"));
        assertNotNull(query);

        assertThat(
                "String Literal will convertTo String Query (But useless",
                query,
                instanceOf(StringQuery.class)
        );

        assertThat(
                "queryStr equal",
                ((StringQuery) query).queryStr,
                is("lfkdsk")
        );
    }

    @Test
    void testUnaryQuery() {
        Query query = visitor.visit(UnaryExpr.of(
                ItemType.itemSUB,
                NumberLiteral.of(100)
        ));

        assertNotNull(query);
        assertThat(
                "Unary Expr Type Checker",
                query,
                instanceOf(StringQuery.class)
        );

        assertThat(
                "queryStr equal",
                ((StringQuery) query).queryStr,
                is("-100")
        );

        query = visitor.visit(UnaryExpr.of(
                ItemType.itemADD,
                NumberLiteral.of(100)
        ));

        assertNotNull(query);
        assertThat(
                "Unary Expr Type Checker",
                query,
                instanceOf(StringQuery.class)
        );

        assertThat(
                "queryStr equal",
                ((StringQuery) query).queryStr,
                is("100")
        );
    }

    @Test
    void testVectorSelector() {
        Query query = visitor.visit(
                VectorSelector.of(
                        "some_metric",
                        newMatcher(
                                Matcher.MatchType.MatchEqual,
                                MetricNameLabel,
                                "some_metric"
                        )
                )
        );

        assertNotNull(query);
        assertThat(
                "Type should be conjunction Query",
                query,
                instanceOf(Conjunction.class)
        );

        EqualityQuery sub = new EqualityQuery(MetricNameLabel, "some_metric");
        Conjunction con = new Conjunction(Collections.singletonList(sub));

        assertThat(
                "Conjunction should be equal",
                ((Conjunction) query).subQueries.size(),
                is(con.subQueries.size())
        );

        EqualityQuery sub1 = (EqualityQuery) ((Conjunction) query).subQueries.get(0);
        assertEquals(sub.col, sub1.col);
        assertEquals(sub.value, sub1.value);
        assertEquals(sub.col, sub1.col);

        query = visitor.visit(
                VectorSelector.of(
                        "some_metric",
                        newMatcher(
                                Matcher.MatchType.MatchNotEqual,
                                MetricNameLabel,
                                "some_metric"
                        )
                )
        );

        assertNotNull(query);
        assertThat(
                "Type should be conjunction Query",
                query,
                instanceOf(Conjunction.class)
        );

        sub = new EqualityQuery(MetricNameLabel, "some_metric", false);
        con = new Conjunction(Collections.singletonList(sub));

        assertThat(
                "Conjunction should be equal",
                ((Conjunction) query).subQueries.size(),
                is(con.subQueries.size())
        );

        sub1 = (EqualityQuery) ((Conjunction) query).subQueries.get(0);
        assertEquals(sub.col, sub1.col);
        assertEquals(sub.value, sub1.value);
        assertEquals(sub.col, sub1.col);
    }

    @Test
    void testVectorSelectorRegex() {
        Query query = visitor.visit(
                VectorSelector.of(
                        "some_metric",
                        newMatcher(
                                Matcher.MatchType.MatchRegexp,
                                MetricNameLabel,
                                "some_metric"
                        )
                )
        );

        assertTrue(query instanceof Conjunction);
        assertEquals(1, ((Conjunction) query).subQueries.size());

        StringQuery sub = (StringQuery) ((Conjunction) query).subQueries.get(0);
        assertEquals(format("_regex:\"%s:%s\"", MetricNameLabel, "some_metric"), sub.queryStr);
    }

    @Test
    void testVectorSelectorNotRegex() {
        Query query = visitor.visit(
                VectorSelector.of(
                        "some_metric",
                        newMatcher(
                                Matcher.MatchType.MatchNotRegexp,
                                MetricNameLabel,
                                "some_metric"
                        )
                )
        );

        assertTrue(query instanceof Conjunction);
        assertEquals(1, ((Conjunction) query).subQueries.size());

        StringQuery sub = (StringQuery) ((Conjunction) query).subQueries.get(0);
        assertEquals(format("_regex:\"(?!%s:%s)\"", MetricNameLabel, "some_metric"), sub.queryStr);
    }

    static class SimpleBinaryTestItem {
        Expr expr;
        String result;

        private SimpleBinaryTestItem(Expr expr, String result) {
            this.expr = expr;
            this.result = result;
        }

        public static SimpleBinaryTestItem of(Expr expr, String result) {
            return new SimpleBinaryTestItem(expr, result);
        }
    }

    @Test
    void testBinaryExpr() {
        SimpleBinaryTestItem[] testItems = new SimpleBinaryTestItem[]{
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemADD,
                                NumberLiteral.of(1),
                                NumberLiteral.of(2)
                        ),
                        "3"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemSUB,
                                NumberLiteral.of(5),
                                NumberLiteral.of(3)
                        ),
                        "2"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemMUL,
                                NumberLiteral.of(5),
                                NumberLiteral.of(3)
                        ),
                        "15"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemDIV,
                                NumberLiteral.of(6),
                                NumberLiteral.of(3)
                        ),
                        "2"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemMOD,
                                NumberLiteral.of(6),
                                NumberLiteral.of(3)
                        ),
                        "0"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemPOW,
                                NumberLiteral.of(2),
                                NumberLiteral.of(3)
                        ),
                        "8"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemEQL,
                                NumberLiteral.of(2),
                                NumberLiteral.of(2)
                        ),
                        "1"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemLSS,
                                NumberLiteral.of(1),
                                NumberLiteral.of(2)
                        ),
                        "1"
                ),
                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemNEQ,
                                NumberLiteral.of(1),
                                NumberLiteral.of(2)
                        ),
                        "1"
                ),

                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemGTR,
                                NumberLiteral.of(2),
                                NumberLiteral.of(1)
                        ),
                        "1"
                ),

                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemGTE,
                                NumberLiteral.of(5),
                                NumberLiteral.of(1)
                        ),
                        "1"
                ),

                SimpleBinaryTestItem.of(
                        BinaryExpr.of(
                                ItemType.itemLTE,
                                NumberLiteral.of(2),
                                NumberLiteral.of(4)
                        ),
                        "1"
                ),
        };


        for (SimpleBinaryTestItem testItem : testItems) {
            Query query = visitor.visit(testItem.expr);
            assertNotNull(query);
            assertTrue(query instanceof StringQuery);

            StringQuery stringQuery = (StringQuery) query;
            assertEquals(testItem.result, stringQuery.queryStr);
        }
    }

    @Test
    void testBinaryExprWithWrongOp() {
        ItemType[] wrongOps = new ItemType[]{
                ItemType.itemLeftBrace,
                ItemType.itemRightBrace,
                ItemType.itemSemicolon,
                ItemType.itemGroupLeft,
                ItemType.itemRightParen
        };

        for (ItemType wrongOp : wrongOps) {
            assertThrows(
                    RuntimeException.class,
                    () -> visitor.visit(
                            BinaryExpr.of(
                                    wrongOp,
                                    NumberLiteral.of(1),
                                    NumberLiteral.of(2)
                            )
                    ),
                    format("operator %s not allowed for Scalar operations", wrongOp.desc()));
        }
    }
}