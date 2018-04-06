package io.dashbase.value;

import io.dashbase.web.response.Response;

public interface Result {
    default Response toResponse() {
        throw new UnsupportedOperationException("UnSupported toResponse Operator in " + this.toString());
    }

    default Result combine(Result other) {
        throw new UnsupportedOperationException("UnSupported Combine Operator in " + this.toString());
    }

    ResultType resultType();

    enum ResultType {
        matrix("matrix"),
        vector("vector"),
        scalar("scalar"),
        string("string");

        private String text;

        ResultType(java.lang.String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
