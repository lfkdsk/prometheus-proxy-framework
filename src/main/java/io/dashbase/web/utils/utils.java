package io.dashbase.web.utils;

public class utils {
    public static boolean isWhiteSpaceOrTabOrNewLine(char ch) {
        return isBlankOrTab(ch) || ch == '\n';
    }

    public static boolean isBlankOrTab(char ch) {
        return ch == ' ' || ch == '\t';
    }

    public static boolean isValidLabelNameStart(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    public static boolean isValidLabelNameContinuation(char ch) {
        return isValidLabelNameStart(ch) || (ch >= '0' && ch <= '9');
    }

    public static boolean isValidMetricNameStart(char ch) {
        return isValidLabelNameStart(ch) || ch == ':';
    }

    public static boolean isValidMetricNameContinuation(char ch) {
        return isValidLabelNameContinuation(ch) || ch == ':';
    }

    public static String getSummaryMetricName(String name) {
        if (isCount(name))
            return name.substring(0, name.length() - 6);
        else if (isSum(name))
            return name.substring(0, name.length() - 4);
        return name;

    }

    public static String getHistogramMetricName(String name) {
        if (isCount(name))
            return name.substring(0, name.length() - 6);
        else if (isSum(name))
            return name.substring(0, name.length() - 4);
        else if (isBucket(name))
            return name.substring(0, name.length() - 7);
        return name;
    }

    public static boolean isCount(String name) {
        int len = name.length();
        return len > 6 && name.substring(len - 6, len).equals("_count");
    }

    public static boolean isSum(String name) {
        int len = name.length();
        return len > 4 && name.substring(len - 4, len).equals("_sum");
    }

    public static boolean isBucket(String name) {
        int len = name.length();
        return len > 7 && name.substring(len - 7, len).equals("_bucket");
    }
}
