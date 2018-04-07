package io.dashbase.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dashbase.value.Result;
import lombok.Data;
import lombok.NonNull;

import static io.dashbase.web.response.Response.StatusType.success;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private StatusType status = success; // "success" | "error"

    private T data;
    // Only set if status is "error". The data field may still hold
    // additional data.
    private ErrorType errorType;

    private String error;

    public enum StatusType {
        success("success"),
        error("error");

        private String text;

        StatusType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum ErrorType {
        errorNone(""),
        errorTimeout("timeout"),
        errorCanceled("canceled"),
        errorExec("execution"),
        errorBadData("bad_data"),
        errorInternal("internal"),
        errorUnavailable("unavailable");

        public String text;

        ErrorType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static Response error(@NonNull ErrorType errorType, @NonNull String error) {
        Response response = new Response();
        response.error = error;
        response.errorType = errorType;
        return response;
    }

    public static <D> Response<D> of(D data) {
        Response<D> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <R> Response<BaseResult<R>> of(@NonNull Result.ResultType resultType, R result) {
        BaseResult<R> base = BaseResult.of(resultType, result);
        return of(base);
    }

    public static Response empty() {
        return new Response();
    }
}
