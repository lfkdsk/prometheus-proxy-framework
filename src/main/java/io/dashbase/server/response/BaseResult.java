package io.dashbase.server.response;

import lombok.Data;

import java.util.List;

@Data
public class BaseResult<E extends BaseResult.QueryResult> {
    private String resultType;

    private List<E> result;

    interface QueryResult {}
}
