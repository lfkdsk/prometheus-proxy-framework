package io.dashbase.utils.collection;

import io.dashbase.value.Result;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public final class ResultCombineCollector implements Collector<Result, List<Result>, Result> {

    @Override
    public Supplier<List<Result>> supplier() {
        // Note: use CopyOnWrite or will occur sync error (lose some point on graph).
        return CopyOnWriteArrayList::new;
    }

    @Override
    public BiConsumer<List<Result>, Result> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<Result>> combiner() {
        return (a, b) -> {
            a.addAll(b);
            return a;
        };
    }

    @Override
    public Function<List<Result>, Result> finisher() {
        return (results) -> {
            Result current = null;
            for (Result result : results) {
                if (Objects.isNull(current) && Objects.nonNull(result)) {
                    current = result;
                    continue;
                }

                if (Objects.isNull(result)) {
                    continue;
                }

                current = current.combine(result);
            }

            return current;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(UNORDERED, CONCURRENT));
    }
}
