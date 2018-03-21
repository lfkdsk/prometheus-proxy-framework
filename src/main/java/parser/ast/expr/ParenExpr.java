package parser.ast.expr;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;

import java.util.Objects;

@ExprBinder(type = ExprType.ParenExpr)
public class ParenExpr extends Expr{

    public Expr inner;

    private ParenExpr(Expr inner) {
        this.inner = inner;
    }

    public static ParenExpr of(Expr expr) {
        return new ParenExpr(expr);
    }

    @Override
    public String toString() {
        return String.format("(%s)", inner.toString());
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof ParenExpr)) {
            return false;
        }

        ParenExpr other = (ParenExpr) obj;
        return hashCode() == other.hashCode();
    }
}
