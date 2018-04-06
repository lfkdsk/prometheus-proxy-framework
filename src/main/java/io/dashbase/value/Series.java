package io.dashbase.value;

import java.util.List;
import java.util.Map;

import static io.dashbase.utils.collection.CollectionUtils.distinct;
import static io.dashbase.utils.collection.CollectionUtils.sort;

public class Series {

    public final List<Point> values;

    public final Map<String, String> metric;

    private Series(List<Point> values, Map<String, String> metrics) {
        this.values = values;
        this.metric = metrics;
    }

    public static Series of(List<Point> values, Map<String, String> metrics) {
        return new Series(values, metrics);
    }

    public Series sorted() {
        List<Point> temp = sort(distinct(values));
        values.clear();
        values.addAll(temp);
        return this;
    }
}
