package parser.ast.expr;

import lexer.token.ItemType;
import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.ast.value.ValueType;
import parser.ast.value.VectorMatching;

import java.util.Objects;

import static java.lang.String.format;
import static parser.ast.value.ValueType.ValueTypeScalar;
import static parser.ast.value.ValueType.ValueTypeVector;

@ExprBinder(type = ExprType.BinaryExpr)
public class BinaryExpr extends Expr {
    public ItemType op;
    public Expr lhs, rhs;
    public VectorMatching vectorMatching;
    public boolean returnBool;

    private BinaryExpr(ItemType op, Expr lhs, Expr rhs, VectorMatching vectorMatching, boolean returnBool) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.vectorMatching = vectorMatching;
        this.returnBool = returnBool;
    }

    public static BinaryExpr of(ItemType op, Expr lhs, Expr rhs) {
        return new BinaryExpr(op, lhs, rhs, null, false);
    }

    public static BinaryExpr of(ItemType op, Expr lhs, Expr rhs, boolean returnBool) {
        return new BinaryExpr(op, lhs, rhs, null, returnBool);
    }

    public static BinaryExpr of(ItemType op, Expr lhs, Expr rhs, boolean returnBool, VectorMatching matching) {
        return new BinaryExpr(op, lhs, rhs, matching, returnBool);
    }

    @Override
    public String toString() {
        return format("BinaryExpr<%s,%s,%s,%s>", op.getText(), lhs, rhs, returnBool);
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

        if (Objects.nonNull(vectorMatching)) {
            if (Objects.isNull(other.vectorMatching)) {
                return false;
            }

            if (!Objects.equals(vectorMatching, other.vectorMatching)) {
                return false;
            }
        }
        // check inner type and hashcode
        return op == other.op
                && lhs.equals(other.lhs)
                && rhs.equals(other.rhs)
                && hashCode() == other.hashCode();
    }

    @Override
    public ValueType valueType() {
        if (lhs.valueType() == ValueTypeScalar && rhs.valueType() == ValueTypeScalar) {
            return ValueTypeScalar;
        }

        return ValueTypeVector;
    }
}
