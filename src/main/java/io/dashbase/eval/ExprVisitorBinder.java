package io.dashbase.eval;

public interface ExprVisitorBinder {
    default <T> T accept(ExprVisitor<T> visitor) {
        throw new UnsupportedOperationException("Unsupported Accept Method");
    }
}
