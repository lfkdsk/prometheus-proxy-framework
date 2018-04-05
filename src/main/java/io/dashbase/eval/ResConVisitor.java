package io.dashbase.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dashbase.eval.binder.ExprVoidVisitor;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;
import io.dashbase.value.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import rapid.api.RapidHit;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;

import java.util.List;
import java.util.Map;

import static io.dashbase.parser.ast.value.VectorSelector.metricName;
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
        List<Series> seriesList = Lists.newArrayList();
        String metricName = metricName(visitor.matchers);

        List<Point> points = Lists.newArrayList();
        Map<String, String> labels = Maps.newHashMap();
        for (RapidHit hit : response.hits) {
            Map<String, List<String>> fields = hit.payload.fields;

            // TODO: labels should get from response
            for (Matcher matcher : visitor.matchers) {
                labels.put(matcher.name, matcher.value);
            }

            List<String> values = fields.getOrDefault(metricName, singletonList("0"));
            Point point = Point.of(hit.timeInSeconds, values.get(0));
            points.add(point);
        }

        Series series = Series.of(points, labels);
        seriesList.add(series);

//        BaseResult<List<Series>> result = new BaseResult<>();
//        result.setResult(metrics);
//        result.setResultType("metrics");
//        Response<BaseResult<List<Series>>> response = new Response<>();
//        response.setData(result);
//        evaluator.setPrometheusRes(response);

        Matrix matrix = Matrix.of(seriesList);
        evaluator.setResult(matrix);
    }

    @Override
    public void visit(VectorSelector visitor) {
        // Note: List of Sample == Vector (result of VectorSelector)
        List<Sample> samples = Lists.newArrayList();
        Vector vector = Vector.of(samples);
        String metricName = metricName(visitor.matchers);

        for (RapidHit hit : response.hits) {
            Map<String, List<String>> fields = hit.payload.fields;
            Map<String, String> metrics = Maps.newHashMap();

            // TODO: labels should get from response
            for (Matcher matcher : visitor.matchers) {
                metrics.put(matcher.name, matcher.value);
            }

            List<String> values = fields.getOrDefault(metricName, singletonList("0"));
            Point point = Point.of(hit.timeInSeconds, values.get(0));
            Sample sample = Sample.of(point, metrics);
            samples.add(sample);
        }

//        BaseResult<List<Sample>> result = new BaseResult<>();
//        result.setResult(samples);
//        result.setResultType("vector");
//        Response<BaseResult<List<Sample>>> response = new Response<>();
//        response.setData(result);
//        evaluator.setPrometheusRes(response);

        evaluator.setResult(vector);
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
