package io.dashbase.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dashbase.PrometheusProxyApplication;
import io.dashbase.eval.binder.ExprVoidVisitor;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.match.Function;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.VectorSelector;
import io.dashbase.value.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import rapid.api.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.dashbase.lexer.token.ItemType.*;
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
        if (visitor.op == itemAvg || visitor.op == itemMin || visitor.op == itemMax) {
            VectorSelector selector = (VectorSelector) visitor.expr;
            String metricName = metricName(selector.matchers);

            AggregationResponse subRes = response.aggregations.get(metricName);
            if (Objects.nonNull(subRes) && subRes instanceof TSAggregationResponse) {
                List<TSAggregationResponse.TSBucket> buckets = ((TSAggregationResponse) subRes).buckets;
                List<Sample> samples = Lists.newArrayList();
                Map<String, String> labels = Maps.newHashMap();

                for (Matcher matcher : selector.matchers) {
                    labels.put(matcher.name, matcher.value);
                }

                labels.put("instance", PrometheusProxyApplication.config.getInstance());
                labels.put("job", PrometheusProxyApplication.config.getJob());

                for (TSAggregationResponse.TSBucket bucket : buckets) {
                    NumericAggregationResponse numeric = (NumericAggregationResponse) bucket.response;
                    Point point = Point.of(bucket.timeInSec, String.valueOf(numeric.value));
                    Sample sample = Sample.of(point, labels);
                    samples.add(sample);
                }

                Vector vector = Vector.of(samples);
                evaluator.setResult(vector);
            }
        }
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

            labels.put("instance", PrometheusProxyApplication.config.getInstance());
            labels.put("job", PrometheusProxyApplication.config.getJob());

            List<String> values = fields.getOrDefault(metricName, singletonList("0"));
            Point point = Point.of(hit.timeInSeconds, values.get(0));
            points.add(point);
        }

        Series series = Series.of(points, labels);
        seriesList.add(series);

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

            metrics.put("instance", PrometheusProxyApplication.config.getInstance());
            metrics.put("job", PrometheusProxyApplication.config.getJob());

            List<String> values = fields.getOrDefault(metricName, singletonList("0"));
            Point point = Point.of(hit.timeInSeconds, values.get(0));
            Sample sample = Sample.of(point, metrics);
            samples.add(sample);
        }

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
        Function function = visitor.function;
        switch (function.name) {
            case "ts": {
                timeSeriesResult(visitor);
                break;
            }
        }
    }

    private void timeSeriesResult(Call visitor) {
        AggregationResponse subRes = response.aggregations.get("ts");
        if (Objects.nonNull(subRes) && subRes instanceof HistogramAggregationResponse) {
            List<HistogramBucket> buckets = ((HistogramAggregationResponse) subRes).histogramBuckets;
            List<Sample> samples = Lists.newArrayList();

            Map<String, String> metrics = Maps.newHashMap();
            metrics.put("instance", PrometheusProxyApplication.config.getInstance());
            metrics.put("job", PrometheusProxyApplication.config.getJob());

            for (HistogramBucket bucket : buckets) {
                Point point = Point.of(bucket.timeInSec, String.valueOf(bucket.count));
                Sample sample = Sample.of(point, metrics);
                samples.add(sample);
            }

            Vector vector = Vector.of(samples);
            evaluator.setResult(vector);
        }
    }
}
