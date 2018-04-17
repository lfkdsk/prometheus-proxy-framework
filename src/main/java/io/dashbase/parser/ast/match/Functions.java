package io.dashbase.parser.ast.match;

import com.google.common.collect.Maps;
import io.dashbase.eval.Evaluator;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.ExprType;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.utils.RapidRequestBuilder;
import rapid.api.TSAggregationRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.dashbase.parser.ast.value.ValueType.*;
import static io.dashbase.utils.TypeUtils.parseDuration;

public final class Functions {
    public static final Map<String, Function> functions = Maps.newHashMap();

    static {
        functions.put("time", Function.of(
                "time",
                Collections.emptyList(),
                0,
                ValueTypeScalar,
                null // TODO : all your function here
        ));


        functions.put("floor", Function.of(
                "floor",
                Arrays.asList(ValueTypeVector),
                0,
                ValueTypeVector,
                null // TODO : all your function here
        ));

        functions.put("rate", Function.of(
                "rate",
                Arrays.asList(ValueTypeMatrix),
                0,
                ValueTypeVector,
                null // TODO : all your function here
        ));

        functions.put("round", Function.of(
                "round",
                Arrays.asList(ValueTypeVector, ValueTypeScalar),
                1,
                ValueTypeVector,
                null // TODO : all your function here
        ));

        functions.put("ts", Function.of(
                "ts",
                Arrays.asList(ValueTypeString, ValueTypeScalar),
                1,
                ValueTypeVector,
                Functions::timeSeries
        ));
    }

    public static Function getFunction(String name) {
        return functions.get(name);
    }

    private static void timeSeries(List<Expr> exprs, Call call, Evaluator evaluator) {
        RapidRequestBuilder builder = evaluator.getRequestBuilder();

        if (exprs.size() <= 0) {
            return;
        }

        Expr timeRange = exprs.get(0);
        TSAggregationRequest ts = new TSAggregationRequest();
        if (timeRange.exprType == ExprType.NumberLiteral) {
            NumberLiteral number = (NumberLiteral) timeRange;
            ts.bucketSize = (int) number.number;
            builder.addAggregation("ts", ts);
        } else if (timeRange.exprType == ExprType.StringLiteral) {
            StringLiteral string = (StringLiteral) timeRange;
            Duration duration = parseDuration(string.string);
            ts.bucketSize = (int) duration.getSeconds();
            builder.addAggregation("ts", ts);
        }
    }
}
