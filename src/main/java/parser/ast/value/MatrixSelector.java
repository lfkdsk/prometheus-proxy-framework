package parser.ast.value;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.match.Matcher;

import java.time.Duration;
import java.util.List;

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

    public static MatrixSelector of(String name, Duration range, List<Matcher> matchers) {
        return new MatrixSelector(name, range, null, matchers);
    }

    @Override
    public ValueType valueType() {
        return ValueTypeMatrix;
    }
}
