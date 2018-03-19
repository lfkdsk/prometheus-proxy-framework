package lexer.state;
import lexer.Lexer;

import static lexer.state.LexerStates.*;
import static token.ItemType.itemNumber;

@StatesBinder(binder = LexNumber)
public class LexNumber extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
        if (!lexer.scanNumber()) {
            return lexer.error("bad number syntax: %q", lexer.current());
        }

        lexer.emit(itemNumber);
        return LexStatements;
    }
}
