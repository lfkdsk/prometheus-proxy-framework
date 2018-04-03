package io.dashbase.parser.ast;

import io.dashbase.eval.binder.ExprVisitor;
import io.dashbase.eval.binder.ExprVisitorBinder;
import io.dashbase.parser.ast.value.ValueType;

public abstract class Expr implements ExprVisitorBinder {
    public final ExprType exprType;

    public Expr() {
        ExprBinder binder = this.getClass().getAnnotation(ExprBinder.class);

        if (binder == null) {
            throw new IllegalArgumentException("Expr should bind to an ExprType annotation");
        }

        exprType = binder.type();
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        throw new UnsupportedOperationException("UnSupported Method");
    }

    public abstract ValueType valueType();
}
