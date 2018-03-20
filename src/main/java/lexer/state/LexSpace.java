package lexer.state;

import lexer.QueryLexer;

import static lexer.state.LexerStates.LexSpace;
import static lexer.state.LexerStates.LexStatements;
import static utils.TypeUtils.isSpace;

@StatesBinder(binder = LexSpace)
public class LexSpace extends State {


    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        for (;isSpace(lexer.peek());) {
            lexer.next();
        }

        lexer.ignore();
        return LexStatements;
    }
}
