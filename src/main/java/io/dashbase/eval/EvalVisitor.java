package io.dashbase.eval;

import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.Values;
import io.dashbase.parser.ast.value.VectorSelector;
import lombok.Getter;

public final class EvalVisitor implements ExprVisitor<Values.Value> {

    @Getter
    private long startTimestamp;

    public EvalVisitor(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public Values.Value visit(AggregateExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(MatrixSelector visitor) {
        return null;
    }

    @Override
    public Values.Value visit(VectorSelector visitor) {
        return null;
    }

    @Override
    public Values.Value visit(BinaryExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(ParenExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(UnaryExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(NumberLiteral visitor) {
        return Values.Scalar.of(startTimestamp, String.valueOf(visitor.number));
    }

    @Override
    public Values.Value visit(StringLiteral visitor) {
        return null;
    }

    @Override
    public Values.Value visit(Call visitor) {
        return null;
    }
}
