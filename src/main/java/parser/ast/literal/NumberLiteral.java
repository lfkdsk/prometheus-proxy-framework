package parser.ast.literal;

import parser.ast.Expr;

public class NumberLiteral extends Expr {
    public double number;

    private NumberLiteral(double number) {
        this.number = number;
    }

    public static NumberLiteral of(double number) {
        return new NumberLiteral(number);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NumberLiteral)) {
            return false;
        }

        NumberLiteral other = (NumberLiteral) obj;
        return Double.valueOf(number).equals(other.number);
    }
}
