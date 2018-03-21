package parser.match;

import parser.ast.Expr;
import parser.ast.value.ValueType;

import java.util.List;

public class Function {
    public String name;
    public List<ValueType> args;
    public int variadic;
    public ValueType returnType;
    public CallFunction call;

    @FunctionalInterface
    public interface CallFunction {
        Expr call(List<Expr> exprs);
    }

    private Function(String name, List<ValueType> args, int variadic, ValueType returnType, CallFunction call) {
        this.name = name;
        this.args = args;
        this.variadic = variadic;
        this.returnType = returnType;
        this.call = call;
    }

    public static Function of(String name, List<ValueType> args, int variadic, ValueType returnType, CallFunction call) {
        return new Function(name, args, variadic, returnType, call);
    }
}
