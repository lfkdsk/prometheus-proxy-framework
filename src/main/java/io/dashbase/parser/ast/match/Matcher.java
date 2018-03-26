package io.dashbase.parser.ast.match;

import java.util.Objects;
import java.util.regex.Pattern;

public class Matcher {
    public enum MatchType {
        MatchEqual("="),
        MatchNotEqual("!="),
        MatchRegexp("=~"),
        MatchNotRegexp("!~");
        public final String text;

        MatchType(String text) {
            this.text = text;
        }
    }

    public MatchType type;
    public String name;
    public String value;
    public Pattern pattern;

    private Matcher(MatchType type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Matcher<%s,%s,%s>", type, name, value);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof Matcher)) {
            return false;
        }

        Matcher other = (Matcher) obj;
        return type == other.type
                && name.equals(other.name)
                && value.equals(other.value)
                && toString().equals(other.toString());
    }

    // NewMatcher returns a matcher object.
    public static Matcher newMatcher(MatchType type, String name, String value) {
        Matcher matcher = new Matcher(
                type, name, value
        );

        if (type == MatchType.MatchRegexp || type == MatchType.MatchNotRegexp) {
            matcher.pattern = Pattern.compile("^(?:" + value + ")$");
        }

        return matcher;
    }

    public boolean matches(String s) {
        switch (type) {
            case MatchEqual: {
                return s.equals(value);
            }
            case MatchRegexp: {
                return Objects.nonNull(pattern) && pattern.matcher(s).matches();
            }

            case MatchNotEqual: {
                return !s.equals(value);
            }

            case MatchNotRegexp: {
                return Objects.nonNull(pattern) && !pattern.matcher(s).matches();
            }
        }

        throw new RuntimeException("labels.Matcher.Matches: invalid match type");
    }
}
