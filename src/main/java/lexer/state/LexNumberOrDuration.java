package lexer.state;

import lexer.Lexer;

import static java.lang.String.format;
import static lexer.state.LexerStates.*;
import static token.ItemType.*;
import static utils.NumberUtils.isAlphaNumeric;

@StatesBinder(binder = LexNumberOrDuration)
public class LexNumberOrDuration extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
        if (lexer.scanNumber()) {
            lexer.emit(itemNumber);
            return LexStatements;
        }

        // Next two chars must be a valid unit and a non-alphanumeric.
        if (lexer.accept("smhdwy")) {
            if (isAlphaNumeric(lexer.next())) {
                return lexer.error("bad number or duration syntax: %s", lexer.current());
            }

            lexer.backup();
            lexer.emit(itemDuration);
            return LexStatements;
        }

        return lexer.error("bad number or duration syntax: %s", lexer.current());
    }
}
