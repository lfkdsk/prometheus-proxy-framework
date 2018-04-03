package io.dashbase.eval.binder;

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
        switch (visitor.exprType) {
            case Call:
                return visit((Call) visitor);
            case ParenExpr:
                return visit((ParenExpr) visitor);
            case UnaryExpr:
                return visit((UnaryExpr) visitor);
            case BinaryExpr:
                return visit((BinaryExpr) visitor);
            case AggregateExpr:
                return visit((AggregateExpr) visitor);
            case NumberLiteral:
                return visit((NumberLiteral) visitor);
            case StringLiteral:
                return visit((StringLiteral) visitor);
            case MatrixSelector:
                return visit((MatrixSelector) visitor);
            case VectorSelector:
                return visit((VectorSelector) visitor);
            default: {
                throw new RuntimeException("Unsupported Expr Type " + visitor.toString());
            }
        }
    }
}
