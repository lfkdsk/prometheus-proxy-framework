package parser.match;

import com.google.common.collect.Maps;

import java.util.Map;

public final class Functions {
    public static final Map<String, Function> functions = Maps.newHashMap();

    static {

    }

    public static Function getFunction(String name) {
        return functions.get(name);
    }
}
