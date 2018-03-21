package parser.ast;

public abstract class Expr {
    public final ExprType type;

    public Expr() {
        ExprBinder binder = this.getClass().getAnnotation(ExprBinder.class);

        if (binder == null) {
            throw new IllegalArgumentException("Expr should bind to an ExprType annotation");
        }

        type = binder.type();
    }
}
