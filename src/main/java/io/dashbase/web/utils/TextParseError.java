package io.dashbase.web.utils;

import lombok.Getter;

public class TextParseError extends RuntimeException {
    @Getter
    String message;

    public TextParseError(String message) {
        this.message = message;
    }
}
