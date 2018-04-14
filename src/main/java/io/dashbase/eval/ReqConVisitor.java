package io.dashbase.eval;

import com.google.common.collect.Lists;
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
import io.dashbase.utils.RapidRequestBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import rapid.api.NumericAggregationRequest;
import rapid.api.TSAggregationRequest;
import rapid.api.query.Conjunction;
import rapid.api.query.EqualityQuery;
import rapid.api.query.Query;
import rapid.api.query.StringQuery;

import java.util.List;

import static io.dashbase.lexer.token.ItemType.*;
import static io.dashbase.parser.ast.match.Labels.MetricName;
import static io.dashbase.parser.ast.value.VectorSelector.metricName;
import static java.lang.String.format;

public final class ReqConVisitor implements ExprVoidVisitor {

    @Getter
    private Evaluator evaluator;

    private RapidRequestBuilder builder;

    private ReqConVisitor(@NotNull Evaluator evaluator) {
        this.evaluator = evaluator;
        this.builder = evaluator.getRequestBuilder();
    }

    public static ReqConVisitor of(Evaluator evaluator) {
        return new ReqConVisitor(evaluator);
    }

    @Override
    public void visit(AggregateExpr visitor) {
        visit(visitor.expr);

        if (visitor.op == itemAvg || visitor.op == itemMax || visitor.op == itemMin) {
            VectorSelector vector = (VectorSelector) visitor.expr;
            String metricName = metricName(vector.matchers);
            TSAggregationRequest baseTS = new TSAggregationRequest();
            baseTS.bucketSize = Math.toIntExact(evaluator.getInterval().getSeconds());

            NumericAggregationRequest avgInSec = new NumericAggregationRequest();
            avgInSec.type = visitor.op.getKey();
            avgInSec.col = metricName;

            baseTS.subRequest = avgInSec;
            builder.addAggregation(metricName, baseTS);
        }
    }

    @Override
    public void visit(MatrixSelector visitor) {
        conSubQueries(visitor.matchers);
        // [5m] -> start - (5m -> 300s)
        long startTime = evaluator.getStart() - visitor.range.getSeconds();
        long endTime = evaluator.getEnd();
        builder.setTimeRangeFilter(startTime, endTime);
    }

    @Override
    public void visit(VectorSelector visitor) {
        conSubQueries(visitor.matchers);
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

    private void conSubQueries(List<Matcher> marchers) {
        List<Query> subQueries = Lists.newArrayList();

        for (Matcher matcher : marchers) {
            if (matcher.name.equals(MetricName)) {
                builder.addFields(matcher.value);
                continue;
            }

            switch (matcher.type) {
                case MatchEqual: {
                    EqualityQuery query = new EqualityQuery(matcher.name, matcher.value);
                    subQueries.add(query);
                    break;
                }

                case MatchNotEqual: {
                    EqualityQuery query = new EqualityQuery(matcher.name, matcher.value, false);
                    subQueries.add(query);
                    break;
                }

                case MatchRegexp: {
                    StringQuery query = new StringQuery(format("_regex:\"%s:%s\"", matcher.name, matcher.value));
                    subQueries.add(query);
                    break;
                }

                case MatchNotRegexp: {
                    StringQuery query = new StringQuery(format("_regex:\"(?!%s:%s)\"", matcher.name, matcher.value));
                    subQueries.add(query);
                    break;
                }
            }
        }

        if (subQueries.size() > 1) {
            Conjunction conjunction = new Conjunction(subQueries);
            builder.addQuery(conjunction);
        }
    }
}
