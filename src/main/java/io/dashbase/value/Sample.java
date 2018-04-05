package io.dashbase.value;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Sample implements Comparable<Sample>{
    public final Point value;
    public final Map<String, String> metric;

    private Sample(Point value, Map<String, String> metrics) {
        this.value = value;
        this.metric = metrics;
    }

    public static Sample of(Point value, Map<String, String> metrics) {
        return new Sample(value, metrics);
    }

    @Override
    public int compareTo(@NotNull Sample o) {
        return (int) (value.timestamp - o.value.timestamp);
    }
}