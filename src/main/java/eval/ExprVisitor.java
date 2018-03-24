package eval;

import parser.ast.Expr;
import parser.ast.expr.BinaryExpr;
import parser.ast.expr.ParenExpr;
import parser.ast.expr.UnaryExpr;
import parser.ast.literal.NumberLiteral;
import parser.ast.literal.StringLiteral;
import parser.ast.value.AggregateExpr;
import parser.ast.value.MatrixSelector;
import parser.ast.value.VectorSelector;
import parser.ast.match.Call;

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
