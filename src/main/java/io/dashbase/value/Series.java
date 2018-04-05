package io.dashbase.value;

import java.util.List;
import java.util.Map;

public class Series {

    public final List<Point> values;

    public final Map<String, String> metrics;

    private Series(List<Point> values, Map<String, String> metrics) {
        this.values = values;
        this.metrics = metrics;
    }

    public static Series of(List<Point> values, Map<String, String> metrics) {
        return new Series(values, metrics);
    }
}
