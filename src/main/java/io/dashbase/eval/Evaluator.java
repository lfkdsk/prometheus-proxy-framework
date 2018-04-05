package io.dashbase.eval;

import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;
import io.dashbase.utils.RapidRequestBuilder;
import io.dashbase.web.response.Response;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;
import rapid.api.query.Query;

import java.util.List;
import java.util.Objects;

import static io.dashbase.PrometheusProxyApplication.httpService;

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
    private long start, end, interval;

    @Getter
    private RapidRequestBuilder requestBuilder;

    @Getter
    private ReqConVisitor reqConVisitor;

    @Getter
    private ResConVisitor resConVisitor;

    @Getter
    private List<Evaluator> subEvaluators;

    private Evaluator(@NonNull String queryString, long start, long end, long interval) {
        this.queryString = queryString;
        this.start = start / 1000;
        this.end = end / 1000;
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

    public Response runInstantQuery() {
        if (start != end) {
            throw new IllegalArgumentException("Instant Query start time should equalTo end time but " + " [start-time] " + start + " [end-time] " + end);
        }

        // Note: end = start + 1 but in prometheus start == end
        requestBuilder.setTimeRangeFilter(start, end + 1);
        return instantQuery();
    }

    public Response runRangeQuery() {
        if (start == end) {
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

        return prometheusRes;
    }

    private Response rangeQuery() {
        return null;
    }
}
