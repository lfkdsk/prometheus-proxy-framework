package io.dashbase.value;

import io.dashbase.web.response.Response;

public interface Result {
    default Response toResponse() {
        return null;
    }

    default Result combine(Result other) {
        return null;
    }

    ResultType resultType();

    enum ResultType {
        Matrix,
        Vector,
        Scalar
    }
}
