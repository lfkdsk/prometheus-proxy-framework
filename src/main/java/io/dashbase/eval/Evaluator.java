package io.dashbase.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;
import io.dashbase.utils.RapidRequestBuilder;
import io.dashbase.value.*;
import io.dashbase.web.response.Response;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static io.dashbase.PrometheusProxyApplication.httpService;
import static io.dashbase.parser.ast.ExprType.AggregateExpr;
import static io.dashbase.utils.collection.CollectionUtils.resultCombine;
import static io.dashbase.utils.collection.CollectionUtils.sort;
import static java.lang.String.format;

public final class Evaluator {
    private final static Logger logger = LoggerFactory.getLogger(Evaluator.class);

    @Getter
    @NonNull
    private String queryString;

    @Getter
    @Setter
    private Expr queryExpr;

    @Getter
    private RapidResponse response;

    @Getter
    private RapidRequest request;

    @Getter
    @Setter
    private Response prometheusRes;

    @Getter
    private long start, end;

    @Getter
    private Duration interval;

    @Getter
    private RapidRequestBuilder requestBuilder;

    @Getter
    @Setter
    private Result result;

    @Getter
    private ReqConVisitor reqConVisitor;

    @Getter
    private ResConVisitor resConVisitor;

    @Getter
    private List<Evaluator> subEvaluators;

    private Evaluator parent;

    private Evaluator(@NonNull String queryString, long start, long end, Duration interval, Evaluator parent) {
        this.queryString = queryString;
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.requestBuilder = RapidRequestBuilder.builder();
        this.request = requestBuilder.getRequest();
        this.parent = parent;
        this.initEvaluator();
    }

    public static Evaluator of(String queryString, long time) {
        return new Evaluator(queryString, time, time, Duration.ofSeconds(1), null);
    }

    public static Evaluator of(String queryString, long time, Evaluator parent) {
        return new Evaluator(queryString, time, time, Duration.ofSeconds(1), parent);
    }

    public static Evaluator of(String queryString, long start, long end, Duration interval) {
        return new Evaluator(queryString, start, end, interval, null);
    }

    private void initEvaluator() {
        requestBuilder.addTableName("_metrics");
        // TODO other default
    }

    private Expr parse() {
        return Objects.isNull(queryExpr) ? Parser.parseExpr(queryString) : queryExpr;
    }

    public Response runInstantQuery() {
        if (start != end) {
            throw new IllegalArgumentException("Instant Query start time should equalTo end time but " + " [start-time] " + start + " [end-time] " + end);
        }

        // Note: end = start + 1 but in prometheus start == end
        requestBuilder.setTimeRangeFilter(start, end + 1);
        return instantQuery();
    }

    public Response runRangeQuery() {
        if (start == end || start > end) {
            throw new IllegalArgumentException("Range Query start time shouldn't equalTo end time but " + " [start-time] " + start + " [end-time] " + end);
        }

        requestBuilder.setTimeRangeFilter(start, end);
        return rangeQuery();
    }

    private Response instantQuery() {
        queryExpr = parse();

        reqConVisitor = ReqConVisitor.of(this);
        reqConVisitor.visit(queryExpr);

        request = requestBuilder.create();

        try {
            response = httpService.query(request);
        } catch (Exception e) {
            logger.error("HttpService Query Error ", e);
        }

        if (Objects.isNull(response)) {
            throw new RuntimeException("HttpService Query Error " + request.toString());
        }

        // get response
        resConVisitor = ResConVisitor.of(this);
        resConVisitor.visit(queryExpr);

        if (Objects.isNull(result)) {
            return Response.empty();
        }

        prometheusRes = result.toResponse();
        return prometheusRes;
    }

    private Response rangeQuery() {
        long intervalSeconds = interval.getSeconds();
        long steps = (end - start) / intervalSeconds;

        queryExpr = parse();

        if (!queryExpr.valueType().isRangeSupport()) {
            throw new IllegalArgumentException(format(
                    "invalid expression type %s for range query, must be Scalar or instant Vector",
                    queryExpr.valueType().documentedType()
            ));
        }

        if (queryExpr.exprType == AggregateExpr) {
            result = simpleAggsRangeQuery();
        } else {
            result = rangeQueryMultiTimes();
        }

        // Note: for type convert
        Series series = refactorOtherResults(result, Series.of(Lists.newArrayList(), Maps.newHashMap()));

        // Note: Reset return type
        if (Objects.isNull(result) || result.resultType() != Result.ResultType.matrix) {
            series = Series.of(
                    sort(series.values),
                    series.metric
            );

            return Response.of(
                    Result.ResultType.matrix,
                    Matrix.of(Lists.newArrayList(series))
            );
        }

        // sorted result
        Matrix matrix = (Matrix) result;
        result = matrix.sorted();

        prometheusRes = result.toResponse();
        return prometheusRes;
    }

    /**
     * Simple Aggs Query Use one dashbase Query
     *
     * @return simple result.
     */
    private Result simpleAggsRangeQuery() {
        requestBuilder.setTimeRangeFilter(start, end);
        instantQuery();
        return getResult();
    }

    // TODO: should support by dashbase

    /**
     * Query Dashbase multi-times with multi-queries.
     * Query split with Interval Seconds
     *
     * @return combine result
     */
    private Result rangeQueryMultiTimes() {
        subEvaluators = Lists.newLinkedList();
        long intervalSeconds = interval.getSeconds();
        // prepare subEvaluator
        for (long start = this.start; start <= end; start += intervalSeconds) {
            Evaluator subEvaluator = Evaluator.of(queryString, start, this);
            subEvaluator.setQueryExpr(queryExpr);
            subEvaluators.add(subEvaluator);
        }

        return subEvaluators.parallelStream()
                            .filter(evaluator -> Objects.nonNull(evaluator.runInstantQuery().getData()))
                            .filter(evaluator -> Objects.nonNull(evaluator.getResult()))
                            .map(Evaluator::getResult)
                            .collect(resultCombine());
    }

    // TODO: if support multi-field input$2 should be Map<String, Series>
    private Series refactorOtherResults(Result result, Series series) {
        if (Objects.isNull(result)) {
            return series;
        }

        switch (result.resultType()) {
            case vector: {
                Vector vector = (Vector) result;
                for (Sample sample : vector.list) {
                    // TODO matrics should be hash => to get multi-type values now we just get same array
                    if (series.metric.size() == 0) {
                        series.metric.putAll(sample.metric);
                    }
                    series.values.add(sample.value);
                }
                break;
            }
        }

        return series;
    }
}
