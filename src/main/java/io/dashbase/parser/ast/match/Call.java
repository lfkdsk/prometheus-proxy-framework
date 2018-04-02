package io.dashbase.parser.ast.match;

import io.dashbase.eval.ExprVisitor;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.ExprBinder;
import io.dashbase.parser.ast.ExprType;
import io.dashbase.parser.ast.value.ValueType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@ExprBinder(type = ExprType.Call)
public class Call extends Expr {
    public Function function;
    public List<Expr> args;

    private Call(Function function, List<Expr> args) {
        this.function = function;
        this.args = args;
    }

    public static Call of(Function function, List<Expr> exprs) {
        return new Call(function, exprs);
    }
    public static Call of(Function function, Expr... exprs) {
        return new Call(function, Arrays.asList(exprs));
    }
    public static Call of(Function function) {
        return new Call(function, Collections.emptyList());
    }

    @Override
    public ValueType valueType() {
        return function.returnType;
    }

    @Override
    public String toString() {
        String exprsStr = args.stream().map(Expr::toString).collect(joining(","));
        return String.format("Call<%s,%s>", exprsStr, function.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof Call)) {
            return false;
        }

        Call other = (Call) obj;
        return hashCode() == other.hashCode();
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
