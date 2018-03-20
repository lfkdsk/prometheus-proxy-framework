package utils;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class TypeUtils {
    private TypeUtils() throws IllegalAccessException {
        throw new IllegalAccessException("illegal constructor");
    }

    public static boolean isSpace(Character r) {
        return Objects.nonNull(r) && (r == ' ' || r == '\t' || r == '\n' || r == '\r');
    }

    /**
     * isEndOfLine reports whether r is an end-of-line character.
     *
     * @param r char
     * @return is end of line
     */
    public static boolean isEndOfLine(Character r) {
        return Objects.nonNull(r) && (r == '\r' || r == '\n');
    }

    /**
     * isDigit reports whether r is a digit. Note: we cannot use unicode.IsDigit()
     * instead because that also classifies non-Latin digits as digits. See
     * https://github.com/prometheus/prometheus/issues/939.
     **/
    public static boolean isDigit(Character r) {
        return Objects.nonNull(r) && ('0' <= r && r <= '9');
    }

    /**
     * isAlpha reports whether r is an alphabetic or underscore.
     **/
    public static boolean isAlpha(Character r) {
        return Objects.nonNull(r) && (r == '_' || ('a' <= r && r <= 'z') || ('A' <= r && r <= 'Z'));
    }

    /**
     * isAlphaNumeric reports whether r is an alphabetic, digit, or underscore.
     */
    public static boolean isAlphaNumeric(Character r) {
        return Objects.nonNull(r) && (isAlpha(r) || isDigit(r));
    }

    public static long count(String text, char search) {
        return text.chars()
                   .mapToObj(c -> (char) c)
                   .filter(c -> c == search)
                   .count();
    }
}
