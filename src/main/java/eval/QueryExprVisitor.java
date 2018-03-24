package eval;

import parser.ast.expr.BinaryExpr;
import parser.ast.expr.ParenExpr;
import parser.ast.expr.UnaryExpr;
import parser.ast.literal.NumberLiteral;
import parser.ast.literal.StringLiteral;
import parser.ast.match.Call;
import parser.ast.value.AggregateExpr;
import parser.ast.value.MatrixSelector;
import parser.ast.value.VectorSelector;
import rapid.api.query.Query;

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
        return null;
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
