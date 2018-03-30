package io.dashbase.eval;

import com.google.common.collect.Lists;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;
import rapid.api.query.Conjunction;
import rapid.api.query.EqualityQuery;
import rapid.api.query.Query;
import rapid.api.query.StringQuery;

import java.util.List;

public final class QueryEvalVisitor implements ExprVisitor<Query> {
    @Override
    public Query visit(AggregateExpr visitor) {
        return null;
    }

    @Override
    public Query visit(MatrixSelector visitor) {
        return null;
    }

    @Override
    public Query visit(VectorSelector visitor) {
        List<Query> queries = Lists.newArrayList();
        for (Matcher matcher : visitor.matchers) {
            switch (matcher.type) {
                case MatchEqual: {
                    EqualityQuery query = new EqualityQuery(matcher.name, matcher.value);
                    queries.add(query);
                    break;
                }

                case MatchNotEqual: {
                    EqualityQuery query = new EqualityQuery(matcher.name, matcher.value, false);
                    queries.add(query);
                    break;
                }

                case MatchRegexp: {
                    // TODO
                }

                case MatchNotRegexp: {
                    // TODO
                }
            }
        }

        return new Conjunction(queries);
    }

    @Override
    public Query visit(BinaryExpr visitor) {
        return null;
    }

    @Override
    public Query visit(ParenExpr visitor) {
        return null;
    }

    @Override
    public Query visit(UnaryExpr visitor) {
        return null;
    }

    @Override
    public Query visit(NumberLiteral visitor) {
        return new StringQuery(String.valueOf(visitor.number));
    }

    @Override
    public Query visit(StringLiteral visitor) {
        return null;
    }

    @Override
    public Query visit(Call visitor) {
        return null;
    }
}
