package parser.ast.literal;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.ast.value.ValueType;

import static parser.ast.value.ValueType.ValueTypeScalar;

@ExprBinder(type = ExprType.NumberLiteral)
public class NumberLiteral extends Expr {
    public double number;

    private NumberLiteral(double number) {
        this.number = number;
    }

    public static NumberLiteral of(double number) {
        return new NumberLiteral(number);
    }

    @Override
    public String toString() {
        return String.format("NumberLiteral<%s>", number);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(number);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NumberLiteral)) {
            return false;
        }

        NumberLiteral other = (NumberLiteral) obj;
        // check number && hashcode
        return hashCode() == other.hashCode()
                && Double.valueOf(number).equals(other.number);
    }

    @Override
    public ValueType valueType() {
        return ValueTypeScalar;
    }
}
