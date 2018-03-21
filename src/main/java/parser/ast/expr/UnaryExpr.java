package parser.ast.expr;

import lexer.token.ItemType;
import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;

import java.util.Objects;

import static java.lang.String.format;

@ExprBinder(type = ExprType.UnaryExpr)
public class UnaryExpr extends Expr{
    public ItemType op;
    public Expr expr;

    private UnaryExpr(ItemType op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

    @Override
    public String toString() {
        return format("UnaryExpr<%s,%s>", op.desc(), expr.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof UnaryExpr)) {
            return false;
        }

        UnaryExpr other = (UnaryExpr) obj;
        return hashCode() == other.hashCode();
    }

    public static UnaryExpr of(ItemType op, Expr expr) {
        return new UnaryExpr(op, expr);
    }
}
