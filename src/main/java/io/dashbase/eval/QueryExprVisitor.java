package io.dashbase.eval;

import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;
import rapid.api.query.Query;
import rapid.api.query.StringQuery;

public class QueryExprVisitor implements ExprVisitor<Query> {
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
        return null;
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
        return new StringQuery(visitor.string);
    }

    @Override
    public Query visit(Call visitor) {
        return null;
    }
}
