package io.dashbase.web.response;

import lombok.Data;

@Data
public class BaseResult<E> {
    private String resultType;

    private E result;

    interface QueryResult {}
}
