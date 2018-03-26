package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;

import static java.lang.String.format;
import static io.dashbase.lexer.token.ItemType.itemDuration;
import static io.dashbase.utils.TypeUtils.isAlphaNumeric;
import static io.dashbase.lexer.state.LexerStates.*;

@StatesBinder(binder = LexDuration)
public class LexDuration extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        if (lexer.scanNumber()) {
            return lexer.error("missing unit character in duration");
        }

        // Next two chars must be a valid unit and a non-alphanumeric.
        if (lexer.accept("smhdwy")) {
            if (isAlphaNumeric(lexer.next())) {
                return lexer.error("bad duration syntax: \"%s\"", lexer.current());
            }

            lexer.backup();
            lexer.emit(itemDuration);
            return LexStatements;
        }

        return lexer.error("bad duration syntax: \"%s\"", lexer.current());
    }
}
