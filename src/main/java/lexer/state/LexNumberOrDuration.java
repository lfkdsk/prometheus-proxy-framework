package lexer.state;

import lexer.QueryLexer;

import static java.lang.String.format;
import static lexer.state.LexerStates.*;
import static lexer.token.ItemType.*;
import static utils.TypeUtils.isAlphaNumeric;

@StatesBinder(binder = LexNumberOrDuration)
public class LexNumberOrDuration extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        if (lexer.scanNumber()) {
            lexer.emit(itemNumber);
            return LexStatements;
        }

        // Next two chars must be a valid unit and a non-alphanumeric.
        if (lexer.accept("smhdwy")) {
            if (isAlphaNumeric(lexer.next())) {
                return lexer.error("bad number or duration syntax: \"%s\"", lexer.current());
            }

            lexer.backup();
            lexer.emit(itemDuration);
            return LexStatements;
        }

        return lexer.error("bad number or duration syntax: \"%s\"", lexer.current());
    }
}
