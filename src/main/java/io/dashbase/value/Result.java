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
