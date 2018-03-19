package lexer.state;

import lexer.Lexer;

import static lexer.state.LexerStates.LexSpace;
import static lexer.state.LexerStates.LexStatements;
import static utils.NumberUtils.isSpace;

@StatesBinder(binder = LexSpace)
public class LexSpace extends State {


    @Override
    public LexerStates nextTo(Lexer lexer) {
        for (;isSpace(lexer.peek());) {
            lexer.next();
        }

        lexer.ignore();
        return LexStatements;
    }
}
