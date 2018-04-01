package io.dashbase.web.utils;

import java.io.Reader;

@TextStateBinder(binder = TextStates.TextStartLabelName)
public class TextStartLabelName extends State {
    @Override
    public TextStates nextTo(TextParser textParser, Reader reader) {
        return textParser.visit(this, reader);
    }
}
