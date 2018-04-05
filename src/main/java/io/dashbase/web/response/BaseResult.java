package io.dashbase.web.response;

import io.dashbase.value.Result;
import lombok.Data;

@Data
public class BaseResult<E> {
    private Result.ResultType resultType;

    private E result;

    interface QueryResult {}

    public static <R> BaseResult<R> of(Result.ResultType resultType, R result) {
        BaseResult<R> base = new BaseResult<>();
        base.setResult(result);
        base.setResultType(resultType);
        return base;
    }
}
