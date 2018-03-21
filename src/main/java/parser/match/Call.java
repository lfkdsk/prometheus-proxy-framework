package parser.match;

import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;
import parser.ast.value.ValueType;

import java.util.List;

@ExprBinder(type = ExprType.Call)
public class Call extends Expr {
    public Function function;
    public List<Expr> exprs;

    private Call(Function function, List<Expr> exprs) {
        this.function = function;
        this.exprs = exprs;
    }

    public static Call of(Function function, List<Expr> exprs) {
        return new Call(function, exprs);
    }

    @Override
    public ValueType valueType() {
        return null;
    }
}
