package lexer.state;

import exception.ParserException;
import lexer.Lexer;
import token.ItemType;

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
                throw new ParserException(format("bad number or duration syntax: %s", lexer.current()));
            }

            lexer.backup();
            lexer.emit(itemDuration);
            return LexStatements;
        }

        throw new ParserException(format("bad number or duration syntax: %s", lexer.current()));
    }
}
