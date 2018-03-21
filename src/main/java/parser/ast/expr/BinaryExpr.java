package parser.ast.expr;

import lexer.token.ItemType;
import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;

import java.util.Objects;

import static java.lang.String.format;

@ExprBinder(type = ExprType.BinaryExpr)
public class BinaryExpr extends Expr {
    public ItemType itemType;
    public Expr lhs, rhs;
    // TODO vector matching
    public boolean returnBool;

    private BinaryExpr(ItemType itemType, Expr lhs, Expr rhs) {
        this.itemType = itemType;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static BinaryExpr of(ItemType op, Expr lhs, Expr rhs) {
        return new BinaryExpr(op, lhs, rhs);
    }

    @Override
    public String toString() {
        return format("BinaryExpr<%s,%s,%s>", itemType.getText(), lhs, rhs);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof BinaryExpr)) {
            return false;
        }

        BinaryExpr other = (BinaryExpr) obj;

        // check inner type and hashcode
        return itemType == other.itemType
                && lhs.equals(other.lhs)
                && rhs.equals(other.rhs)
                && hashCode() == other.hashCode();
    }
}
