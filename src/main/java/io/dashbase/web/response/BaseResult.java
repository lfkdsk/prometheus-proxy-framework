package io.dashbase.web.response;

import io.dashbase.parser.ast.value.Values;
import lombok.Data;

@Data
public class BaseResult<E extends Values.Value> {
    private String resultType;

    private E result;

    interface QueryResult {}
}
