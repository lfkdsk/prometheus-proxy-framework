package io.dashbase.utils;

public final class Strings {
    // Unquote interprets s as a single-quoted, double-quoted, or backquoted
    // Prometheus query language string literal, returning the string value that s
    // quotes.
    //
    // NOTE: This function as well as the necessary helper functions below
    // (unquoteChar, contains, unhex) and associated tests have been adapted from
    // the corresponding functions in the "strconv" package of the Go standard
    // library to work for Prometheus-style strings. Go's special-casing for single
    // quotes was removed and single quoted strings are now treated the same as
    // double quoted ones.
    public static String unquote(String text) {
        int len = text.length();
        if (len < 2) {
            return text;
        }

        char quote = text.charAt(0);
        if (quote != text.charAt(len - 1)) {
            return text;
        }

        String inner = text.substring(1, len - 1);
        if (quote == '`') {
            if (inner.contains("`")) {
                throw new RuntimeException("invalid syntax");
            }

            return inner;
        }

        if (quote != '\'' && quote != '\"') {
            throw new RuntimeException("invalid syntax");
        }

        if (inner.contains("\n")) {
            throw new RuntimeException("invalid syntax");
        }

        if (!(inner.contains("\\")) && !(inner.contains(String.valueOf(quote)))) {
            return inner;
        }

        return inner
//                .replaceAll("\\\\([\"])", "$1")
                .replaceAll("\\\\([\'])", "$1")
                .replaceAll("\\\\([a-f])]", "\\($1)");
    }
}
