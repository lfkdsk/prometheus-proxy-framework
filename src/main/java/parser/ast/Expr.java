package parser.ast;

import eval.ExprVisitor;
import eval.ExprVisitorBinder;
import parser.ast.value.ValueType;

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
        return visitor.visit(this);
    }

    public abstract ValueType valueType();
}
