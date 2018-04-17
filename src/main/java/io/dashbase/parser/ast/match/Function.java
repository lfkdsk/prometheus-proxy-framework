package io.dashbase.parser.ast.match;

import io.dashbase.eval.Evaluator;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.value.ValueType;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class Function {
    public String name;
    public List<ValueType> argsTypes;
    public int variadic;
    public ValueType returnType;
    public CallFunction call;

    @FunctionalInterface
    public interface CallFunction {
        void call(List<Expr> exprs, Call value, Evaluator evaluator);
    }

    private Function(String name, List<ValueType> args, int variadic, ValueType returnType, CallFunction call) {
        this.name = name;
        this.argsTypes = args;
        this.variadic = variadic;
        this.returnType = returnType;
        this.call = call;
    }

    public static Function of(String name, List<ValueType> args, int variadic, ValueType returnType, CallFunction call) {
        return new Function(name, args, variadic, returnType, call);
    }

    @Override
    public String toString() {
        String argsTypes = this.argsTypes.stream().map(ValueType::documentedType).collect(joining(","));
        String returnTypes = returnType.documentedType();
        return String.format("Func<%s,%s,%s>", name, argsTypes, returnTypes);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof Function)) {
            return false;
        }

        Function other = (Function) obj;
        return hashCode() == other.hashCode();
    }

    public void addTo(@NonNull Map<String, Function> funcs) {
        funcs.put(name, this);
    }
}
