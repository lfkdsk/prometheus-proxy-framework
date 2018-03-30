package io.dashbase.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private String status = "success"; // "success" | "error"

    private T data;
    // Only set if status is "error". The data field may still hold
    // additional data.
    private String errorType;

    private String error;
}
