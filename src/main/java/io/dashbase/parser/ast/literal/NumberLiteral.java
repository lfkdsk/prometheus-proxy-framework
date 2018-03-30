package io.dashbase.parser.ast.literal;

import io.dashbase.eval.ExprVisitor;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.ExprBinder;
import io.dashbase.parser.ast.ExprType;
import io.dashbase.parser.ast.value.ValueType;

import static io.dashbase.parser.ast.value.ValueType.ValueTypeScalar;

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

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
