package io.dashbase.eval.binder;

public interface ExprVisitorBinder {
    default <T> T accept(ExprVisitor<T> visitor) {
        throw new UnsupportedOperationException("Unsupported Accept Method");
    }
}
