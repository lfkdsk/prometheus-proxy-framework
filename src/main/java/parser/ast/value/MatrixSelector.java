package parser.ast.value;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.match.Matcher;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static parser.ast.value.ValueType.ValueTypeMatrix;

// MatrixSelector represents a Matrix selection.
@ExprBinder(type = ExprType.MatrixSelector)
public class MatrixSelector extends Expr {
    public String name;
    public Duration range;
    public Duration offset;
    public List<Matcher> matchers;

    private MatrixSelector(String name, Duration range, Duration offset, List<Matcher> matchers) {
        this.name = name;
        this.range = range;
        this.offset = offset;
        this.matchers = matchers;
    }

    public static MatrixSelector of(String name, Duration range, Duration offset, List<Matcher> matchers) {
        return new MatrixSelector(name, range, offset, matchers);
    }

    public static MatrixSelector of(String name, Duration range, Duration offset, Matcher... matchers) {
        return new MatrixSelector(name, range, offset, Arrays.asList(matchers));
    }

    public static MatrixSelector of(String name, Duration range, List<Matcher> matchers) {
        return new MatrixSelector(name, range, Duration.ZERO, matchers);
    }

    @Override
    public ValueType valueType() {
        return ValueTypeMatrix;
    }

    @Override
    public String toString() {
        String matcherStr = matchers.stream().map(Matcher::toString).collect(joining(","));

        return String.format(
                "MatrixSelector<%s,%s,%s,%s>",
                name,
                range.toString(),
                offset.toString(),
                matcherStr
        );
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof MatrixSelector)) {
            return false;
        }

        MatrixSelector other = (MatrixSelector) obj;
        return hashCode() == other.hashCode();
    }
}
