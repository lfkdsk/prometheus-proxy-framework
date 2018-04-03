package io.dashbase.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dashbase.PrometheusConfig;
import io.dashbase.PrometheusProxyApplication;
import io.dashbase.eval.binder.ExprVoidVisitor;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.match.Labels;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;
import io.dashbase.utils.RapidRequestBuilder;
import io.dashbase.value.Values;
import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.Response;
import lombok.Getter;
import org.apache.kafka.common.MetricName;
import org.jetbrains.annotations.NotNull;
import rapid.api.RapidHit;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.dashbase.parser.ast.value.VectorSelector.metricName;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public final class ResConVisitor implements ExprVoidVisitor {
    @Getter
    private Evaluator evaluator;

    private RapidResponse response;

    private RapidRequest request;

    private ResConVisitor(@NotNull Evaluator evaluator) {
        this.evaluator = evaluator;
        this.response = evaluator.getResponse();
        this.request = evaluator.getRequest();
    }

    public static ResConVisitor of(Evaluator evaluator) {
        return new ResConVisitor(evaluator);
    }

    @Override
    public void visit(AggregateExpr visitor) {

    }

    @Override
    public void visit(MatrixSelector visitor) {

    }

    @Override
    public void visit(VectorSelector visitor) {
        List<Values.Sample> samples = Lists.newArrayList();
        String metricName = metricName(visitor);

        for (RapidHit hit : response.hits) {
            Map<String, List<String>> fields = hit.payload.fields;
            Map<String, String> metrics = Maps.newHashMap();

            // TODO: labels should get from response
            for (Matcher matcher : visitor.matchers) {
                metrics.put(matcher.name, matcher.value);
            }

            List<String> values = fields.getOrDefault(metricName, singletonList("0"));
            Values.Point point = Values.Point.of(hit.timeInSeconds, values.get(0));
            Values.Sample sample = Values.Sample.of(point, metrics);
            samples.add(sample);
        }

        BaseResult<List<Values.Sample>> result = new BaseResult<>();
        result.setResult(samples);
        result.setResultType("vector");
        Response<BaseResult<List<Values.Sample>>> response = new Response<>();
        response.setData(result);
        evaluator.setPrometheusRes(response);
    }

    @Override
    public void visit(BinaryExpr visitor) {

    }

    @Override
    public void visit(ParenExpr visitor) {

    }

    @Override
    public void visit(UnaryExpr visitor) {

    }

    @Override
    public void visit(NumberLiteral visitor) {

    }

    @Override
    public void visit(StringLiteral visitor) {

    }

    @Override
    public void visit(Call visitor) {

    }
}
