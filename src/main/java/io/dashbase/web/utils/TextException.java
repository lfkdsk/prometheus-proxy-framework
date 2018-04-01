package io.dashbase.web.utils;


import java.io.Reader;

@TextStateBinder(binder = TextStates.TextTerminal)
public class TextException extends State {

    @Override
    public TextStates nextTo(TextParser textParser, Reader reader) {
        return TextStates.TextException;
    }

}