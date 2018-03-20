package lexer.state;
import lexer.QueryLexer;

import static lexer.state.LexerStates.*;
import static token.ItemType.itemNumber;

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
