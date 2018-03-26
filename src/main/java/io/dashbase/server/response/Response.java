package io.dashbase.server.response;

import lombok.Data;

import java.util.List;

@Data
public class Response<T> {
    private String status; // "success" | "error"

    private T data;
    // Only set if status is "error". The data field may still hold
    // additional data.
    private String errorType;

    private String error;
}
