package io.dashbase.utils;

import java.util.Collections;
import java.util.List;

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
}
