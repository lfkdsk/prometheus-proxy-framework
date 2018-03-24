package parser.ast.value;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.ast.match.Matcher;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@ExprBinder(type = ExprType.VectorSelector)
public class VectorSelector extends Expr {
    public String name;
    public Duration offset;
    public List<Matcher> matchers;

    private VectorSelector(String name, List<Matcher> matchers, Duration offset) {
        this.name = name;
        this.matchers = matchers;
        this.offset = offset;
    }

    @Override
    public String toString() {
        String matcherStr = matchers.stream()
                                    .map(Matcher::toString)
                                    .collect(joining(","));
        return String.format("VectorSelector<%s,%s>", name, matcherStr);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof VectorSelector)) {
            return false;
        }

        VectorSelector other = (VectorSelector) obj;
        return hashCode() == other.hashCode();
    }

    public static VectorSelector of(String name) {
        return new VectorSelector(name, Collections.emptyList(), null);
    }

    public static VectorSelector of(String name, List<Matcher> matchers) {
        return new VectorSelector(name, matchers, null);
    }

    public static VectorSelector of(String name, Matcher... matchers) {
        return new VectorSelector(name, Arrays.asList(matchers), null);
    }

    public static VectorSelector of(String name, Duration offset, Matcher... matchers) {
        return new VectorSelector(name, Arrays.asList(matchers), offset);
    }

    @Override
    public ValueType valueType() {
        return ValueType.ValueTypeVector;
    }
}
