package io.dashbase.eval;

import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;
import io.dashbase.parser.ast.match.Call;

public interface ExprVisitor<T> {
    T visit(AggregateExpr visitor);

    T visit(MatrixSelector visitor);

    T visit(VectorSelector visitor);

    T visit(BinaryExpr visitor);

    T visit(ParenExpr visitor);

    T visit(UnaryExpr visitor);

    T visit(NumberLiteral visitor);

    T visit(StringLiteral visitor);

    T visit(Call visitor);

    default T visit(Expr visitor) {
        throw new UnsupportedOperationException("Unsupported Eval in Abstract Expr");
    }
}
