package io.dashbase.web.converter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dashbase.eval.EvalVisitor;
import io.dashbase.eval.QueryEvalVisitor;
import io.dashbase.merge.util.CountAggregationMerger;
import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.value.Values;
import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.Response;
import lombok.Getter;
import rapid.api.*;
import rapid.api.query.EqualityQuery;
import rapid.api.query.Query;

public final class ResponseFactory {

    @Getter
    private String queryString;

    @Getter
    private Expr queryExpr;

    @Getter
    private Query query;

    @Getter
    private Values.Value value;

    @Getter
    private RapidRequest rapidRequest;

    @Getter
    private RapidResponse response;

    @Getter
    private long start, end, interval;

    private ResponseFactory(String queryString, long start, long end, long interval) {
        this.queryString = queryString;
        this.start = start;
        this.end = end;
        this.interval = interval;
    }

    public static ResponseFactory of(String queryString, long start) {
        return new ResponseFactory(queryString, start, start, 0);
    }

    public RapidRequest createRequest() {
        rapidRequest = new RapidRequest();
        queryExpr = Parser.parseExpr(queryString);
        query = queryExpr.accept(new QueryEvalVisitor(this));
        rapidRequest.tableNames = Sets.newHashSet("_metrics");
//        rapidRequest.query = new EqualityQuery("__name__", "jvm.cpu.usage.percent.value");
        rapidRequest.timeRangeFilter = new TimeRangeFilter();
        rapidRequest.timeRangeFilter.startTimeInSec = 1522670146;
        rapidRequest.timeRangeFilter.endTimeInSec = 1522670206;
        return rapidRequest;
    }

    public Response<BaseResult<Values.Value>> instantQuery() {
        Values.Value scalar = queryExpr.accept(new EvalVisitor(this));
        BaseResult<Values.Value> result = new BaseResult<>();
        result.setResult(scalar);
        result.setResultType(queryExpr.valueType().getText());
        Response<BaseResult<Values.Value>> response = new Response<>();
        response.setData(result);

        return response;
    }
}
