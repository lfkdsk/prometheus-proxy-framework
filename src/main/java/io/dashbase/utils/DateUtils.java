package io.dashbase.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class DateUtils {
    private DateUtils() {

    }

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static String currentTime() {
        return dateTimeFormatter.print(System.currentTimeMillis());
    }

    public static long timeNum(String timestamp) {
        return dateTimeFormatter.withZoneUTC().parseMillis(timestamp);
    }
}
