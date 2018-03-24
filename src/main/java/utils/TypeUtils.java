package utils;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
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

    // isLabel reports whether the string can be used as label.
    public static boolean isLabel(String text) {
        if (text.length() == 0 || !isAlpha(text.charAt(0))) {
            return false;
        }

        for (char c : text.substring(1).toCharArray()) {
            if (!isAlphaNumeric(c)) {
                return false;
            }
        }

        return true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Other Utils
    ///////////////////////////////////////////////////////////////////////////

    public static long count(String text, char search) {
        return text.chars()
                   .mapToObj(c -> (char) c)
                   .filter(c -> c == search)
                   .count();
    }

    public static Pattern durationRE = Pattern.compile("^([0-9]+)(y|w|d|h|m|s|ms)$");

    public static Duration parseDuration(String durationStr) {
        Matcher matches = durationRE.matcher(durationStr);

        if (matches.groupCount() != 2 || !matches.find()) {
            throw new RuntimeException(format("not a valid duration string: %s", durationStr));
        }

        int n = Integer.valueOf(matches.group(1));
        //	Nanosecond  Duration = 1
        //	Microsecond          = 1000 * Nanosecond
        //	Millisecond          = 1000 * Microsecond
        //	Second               = 1000 * Millisecond
        //	Minute               = 60 * Second
        //	Hour                 = 60 * Minute
        Duration dur = Duration.ZERO; // millisecond
        String unit = matches.group(2);

        switch (unit) {
            case "y":
                dur = dur.plusDays(n * 365);
                break;
            case "w":
                dur = dur.plusDays(n * 7);
                break;
            case "d":
                dur = dur.plusDays(n);
                break;
            case "h":
                dur = dur.plusHours(n);
                break;
            case "m":
                dur = dur.plusMinutes(n);
                break;
            case "s":
                dur = dur.plusSeconds(n);
                break;
            case "ms":
                // Value already correct
                break;
            default:
                throw new RuntimeException(format("invalid time unit in duration string: %s", unit));
        }

        // return millisecond
        return dur;
    }
}
