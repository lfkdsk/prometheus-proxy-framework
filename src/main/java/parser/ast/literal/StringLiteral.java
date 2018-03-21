package parser.ast.literal;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.ast.value.ValueType;

import java.util.Objects;

import static java.lang.String.format;

@ExprBinder(type = ExprType.StringLiteral)
public class StringLiteral extends Expr {

    public final String string;

    private StringLiteral(String string) {
        this.string = string;
    }

    public static StringLiteral of(String string) {
        return new StringLiteral(string);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return format("StringLiteral<%s>", string);
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof StringLiteral)) {
            return false;
        }

        StringLiteral other = (StringLiteral) obj;
        return hashCode() == other.hashCode();
    }

    @Override
    public ValueType valueType() {
        return ValueType.ValueTypeString;
    }
}
