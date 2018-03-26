package io.dashbase.lexer.state;
import io.dashbase.lexer.QueryLexer;

import static io.dashbase.lexer.state.LexerStates.*;
import static io.dashbase.lexer.token.ItemType.itemNumber;

@StatesBinder(binder = LexNumber)
public class LexNumber extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        if (!lexer.scanNumber()) {
            return lexer.error("bad number syntax: %q", lexer.current());
        }

        lexer.emit(itemNumber);
        return LexStatements;
    }
}
