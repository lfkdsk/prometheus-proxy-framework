package lexer.state;

import exception.ParserException;
import lexer.Lexer;

import static java.lang.String.format;
import static token.ItemType.itemDuration;
import static utils.NumberUtils.isAlphaNumeric;
import static lexer.state.LexerStates.*;

@StatesBinder(binder = LexDuration)
public class LexDuration extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
        if (lexer.scanNumber()) {
            throw new ParserException("missing unit character in duration");
        }

        // Next two chars must be a valid unit and a non-alphanumeric.
        if (lexer.accept("smhdwy")) {
            if (isAlphaNumeric(lexer.next())) {
                throw new ParserException(format("bad duration syntax: %s", lexer.current()));
            }

            lexer.backup();
            lexer.emit(itemDuration);
            return LexStatements;
        }

        throw new ParserException(format("bad duration syntax: %s", lexer.current()));
    }
}
