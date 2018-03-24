package parser.ast.match;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static parser.ast.value.ValueType.ValueTypeMatrix;
import static parser.ast.value.ValueType.ValueTypeScalar;
import static parser.ast.value.ValueType.ValueTypeVector;

public final class Functions {
    public static final Map<String, Function> functions = Maps.newHashMap();

    static {
        functions.put("time", Function.of(
                "time",
                Collections.emptyList(),
                0,
                ValueTypeScalar,
                exprs -> null // TODO : all your function here
        ));


        functions.put("floor", Function.of(
                "floor",
                Arrays.asList(ValueTypeVector),
                0,
                ValueTypeVector,
                exprs -> null // TODO : all your function here
        ));

        functions.put("rate", Function.of(
                "rate",
                Arrays.asList(ValueTypeMatrix),
                0,
                ValueTypeVector,
                exprs -> null // TODO : all your function here
        ));

        functions.put("round", Function.of(
                "round",
                Arrays.asList(ValueTypeVector, ValueTypeScalar),
                1,
                ValueTypeVector,
                exprs -> null // TODO : all your function here
        ));
    }

    public static Function getFunction(String name) {
        return functions.get(name);
    }
}
