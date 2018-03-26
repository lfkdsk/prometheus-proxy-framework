package io.dashbase.parser.ast.expr;

import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.ExprBinder;
import io.dashbase.parser.ast.ExprType;
import io.dashbase.parser.ast.value.ValueType;

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

    @Override
    public ValueType valueType() {
        return inner.valueType();
    }
}
