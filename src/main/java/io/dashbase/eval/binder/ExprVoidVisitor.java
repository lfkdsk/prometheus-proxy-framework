package io.dashbase.eval.binder;

import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;

public interface ExprVoidVisitor {

    void visit(AggregateExpr visitor);

    void visit(MatrixSelector visitor);

    void visit(VectorSelector visitor);

    void visit(BinaryExpr visitor);

    void visit(ParenExpr visitor);

    void visit(UnaryExpr visitor);

    void visit(NumberLiteral visitor);

    void visit(StringLiteral visitor);

    void visit(Call visitor);

    default void visit(Expr visitor) {
        switch (visitor.exprType) {
            case Call:
                visit((Call) visitor); break;
            case ParenExpr:
                visit((ParenExpr) visitor); break;
            case UnaryExpr:
                visit((UnaryExpr) visitor); break;
            case BinaryExpr:
                visit((BinaryExpr) visitor); break;
            case AggregateExpr:
                visit((AggregateExpr) visitor); break;
            case NumberLiteral:
                visit((NumberLiteral) visitor); break;
            case StringLiteral:
                visit((StringLiteral) visitor); break;
            case MatrixSelector:
                visit((MatrixSelector) visitor); break;
            case VectorSelector:
                visit((VectorSelector) visitor); break;
            default: {
                throw new RuntimeException("Unsupported Expr Type " + visitor.toString());
            }
        }
    }
}
