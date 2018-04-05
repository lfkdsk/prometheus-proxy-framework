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
import static io.dashbase.utils.CollectionUtils.distinct;
import static io.dashbase.utils.CollectionUtils.sort;

public final class Evaluator {
    private final static Logger logger = LoggerFactory.getLogger(Evaluator.class);

    @Getter
    @NonNull
    private String queryString;

    @Getter
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

    private Evaluator(@NonNull String queryString, long start, long end, Duration interval) {
        this.queryString = queryString;
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.requestBuilder = RapidRequestBuilder.builder();
        this.request = requestBuilder.getRequest();
        this.initEvaluator();
    }

    public static Evaluator of(String queryString, long time) {
        return new Evaluator(queryString, time, time, Duration.ofSeconds(1));
    }

    public static Evaluator of(String queryString, long start, long end, Duration interval) {
        return new Evaluator(queryString, start, end, interval);
    }

    private void initEvaluator() {
        requestBuilder.addTableName("_metrics");
        // TODO other default
    }

    private Expr parse() {
        return Parser.parseExpr(queryString);
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

        subEvaluators = Lists.newLinkedList();

        Series series = Series.of(Lists.newArrayList(), Maps.newHashMap());

        for (long start = this.start; start <= end; start += intervalSeconds) {
            Evaluator subEvaluator = Evaluator.of(queryString, start);
            subEvaluators.add(subEvaluator);
            // run query
            subEvaluator.runInstantQuery();
            Result subResult = subEvaluator.getResult();

            if (Objects.isNull(result)) {
                result = subResult;
                continue;
            } else {
                result = result.combine(subResult);
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
        }

        if (Objects.isNull(result) || result.resultType() != Result.ResultType.matrix) {

            series = Series.of(
                    sort(distinct(series.values)),
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
}
