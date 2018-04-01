package io.dashbase.web.utils;

import lombok.Getter;

import java.io.Reader;

public abstract class State {

    @Getter
    private TextStates textStates;

    State() {
        TextStateBinder binder = this.getClass().getAnnotation(TextStateBinder.class);
        if (binder == null) {
            throw new IllegalArgumentException("State Class Should bind TextStates");
        }

        this.textStates = binder.binder();
    }

    abstract public TextStates nextTo(TextParser textParser, Reader reader);

}
