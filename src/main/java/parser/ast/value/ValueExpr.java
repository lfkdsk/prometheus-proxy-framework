package parser.ast.value;

import parser.ast.Expr;

public abstract class ValueExpr extends Expr {
    abstract ValueType type();
}
