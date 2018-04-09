package io.dashbase.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dashbase.value.Result;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private String status = "success"; // "success" | "error"

    private T data;
    // Only set if status is "error". The data field may still hold
    // additional data.
    private ErrorType errorType;

    private String error;

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

    public static Response error(ErrorType errorType, String error) {
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

    public static <R> Response<BaseResult<R>> of(Result.ResultType resultType, R result) {
        BaseResult<R> base = BaseResult.of(resultType, result);
        return of(base);
    }

    public static Response empty() {
        return new Response();
    }

    public static <R> Response<R> empty(R emptyData) {
        Response<R> response = new Response<>();
        response.setData(emptyData);
        return response;
    }
}
