package io.dashbase.web.utils;

import java.io.Reader;

@TextStateBinder(binder = TextStates.TextReadNewLine)
public class TextReadNewLine extends State {
    @Override
    public TextStates nextTo(TextParser textParser, Reader reader) {
        return textParser.visit(this, reader);
    }
}
