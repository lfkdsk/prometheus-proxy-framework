package io.dashbase.utils.collection;

import io.dashbase.value.Result;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

public final class CollectionUtils {

    private CollectionUtils() throws IllegalAccessException {
        throw new IllegalAccessException("illegal access constructor");
    }

    public static <T> List<T> distinct(List<T> origin) {
        return origin.stream().distinct().collect(toList());
    }

    public static <T extends Comparable<? super T>> List<T> sort(List<T> list) {
        Collections.sort(list);
        return list;
    }

    public static Collector<Result, List<Result>, Result> resultCombine() {
        return new ResultCombineCollector();
    }
}
