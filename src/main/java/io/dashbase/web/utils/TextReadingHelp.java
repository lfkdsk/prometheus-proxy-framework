package io.dashbase.web.utils;

import java.io.Reader;

@TextStateBinder(binder = TextStates.TextReadingHelp)
public class TextReadingHelp extends State {
    @Override
    public TextStates nextTo(TextParser textParser, Reader reader) {
        return textParser.visit(this, reader);
    }
}
