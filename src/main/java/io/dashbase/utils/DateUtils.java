package io.dashbase.utils;

import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static io.dashbase.utils.TypeUtils.isLong;

public final class DateUtils {
    private DateUtils() throws IllegalAccessException {
        throw new IllegalAccessException("illegal constructor");
    }

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static String currentTime() {
        return dateTimeFormatter.print(System.currentTimeMillis());
    }

    public static long timeNum(@NotNull String timestamp) {
        if (isLong(timestamp)) {
            return Long.parseLong(timestamp);
        }

        return dateTimeFormatter.withZoneUTC().parseMillis(timestamp);
    }
}
