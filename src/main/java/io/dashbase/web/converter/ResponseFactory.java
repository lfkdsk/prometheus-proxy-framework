package io.dashbase.web.converter;

import com.google.common.collect.Sets;
import io.dashbase.eval.Evaluator;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.value.Values;
import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.Response;
import lombok.Getter;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;
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
        queryExpr = Evaluator.expr(queryString);
        query = Evaluator.query(queryExpr);
        rapidRequest = new RapidRequest();
        rapidRequest.tableNames = Sets.newHashSet("nginx_json");
        rapidRequest.query = query;
        return rapidRequest;
    }

    public Response<BaseResult<Values.Value>> instantQuery() {
        Values.Value scalar = Evaluator.eval(queryExpr, start);
        BaseResult<Values.Value> result = new BaseResult<>();
        result.setResult(scalar);
        result.setResultType(queryExpr.valueType().getText());
        Response<BaseResult<Values.Value>> response = new Response<>();
        response.setData(result);

        return response;
    }
}
