package parser.ast;

import parser.ast.value.ValueType;

public abstract class Expr {
    public final ExprType exprType;

    public Expr() {
        ExprBinder binder = this.getClass().getAnnotation(ExprBinder.class);

        if (binder == null) {
            throw new IllegalArgumentException("Expr should bind to an ExprType annotation");
        }

        exprType = binder.type();
    }

    public abstract ValueType valueType();
}
