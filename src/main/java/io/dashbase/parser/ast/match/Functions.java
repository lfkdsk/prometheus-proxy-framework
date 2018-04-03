package io.dashbase.parser.ast.match;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static io.dashbase.parser.ast.value.ValueType.*;

public final class Functions {
    public static final Map<String, Function> functions = Maps.newHashMap();

//    public final static Function.CallFunction<AggregationRequest, ResponseFactory>
//            avgOverTime = (exprs, factory) -> {
//
//        NumericAggregationRequest request = new NumericAggregationRequest();
//        request.type = "avg";
//        request.col = ((VectorSelector) exprs.get(0)).name;
//
//        RapidRequest rapidRequest = factory.getRapidRequest();
//        rapidRequest.aggregations.put(request.col, request);
//        return request;
//    };

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

        functions.put("avgs", Function.of(
                "avgs",
                Collections.singletonList(ValueTypeVector),
                0,
                ValueTypeVector,
                null
        ));
    }

    public static Function getFunction(String name) {
        return functions.get(name);
    }
}
