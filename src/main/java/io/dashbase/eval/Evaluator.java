package io.dashbase.eval;

import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.value.Values;
import io.dashbase.utils.RapidRequestBuilder;
import lombok.Getter;
import lombok.NonNull;
import rapid.api.NumericAggregationRequest;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;
import rapid.api.TSAggregationRequest;
import rapid.api.query.Query;

import java.util.Objects;

public final class Evaluator {
    @Getter
    @NonNull
    private String queryString;

    @Getter
    private Expr queryExpr;

    @Getter
    private Query query;

    @Getter
    private RapidResponse response;

    @Getter
    private long start, end, interval;

    @Getter
    private RapidRequestBuilder requestBuilder;

    @Getter
    @NonNull
    private RapidRequest request;

    private Evaluator(@NonNull String queryString, long start, long end, long interval) {
        this.queryString = queryString;
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.requestBuilder = RapidRequestBuilder.builder();
        this.request = requestBuilder.getRequest();
        this.initEvaluator();
    }

    public static Evaluator of(String queryString, long time) {
        return new Evaluator(queryString, time, time, 0);
    }

    public static Evaluator of(String queryString, long start, long end) {
        return new Evaluator(queryString, start, end, end - start);
    }

    private void initEvaluator() {
        requestBuilder.addTableName("_metrics");
        // TODO other default
    }

    public Expr parse() {
        return Parser.parseExpr(queryString);
    }

    public void runInstantQuery() {
        if (start != end) {
            throw new IllegalArgumentException("Instant Query start time should equalTo end time but " + " [start-time] " + start + " [end-time] " + end);
        }

        // Note: end = start + 1 but in prometheus start == end
        requestBuilder.setTimeRangeFilter(start, end + 1);

        queryExpr = parse();


    }
}
