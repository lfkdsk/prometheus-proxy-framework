package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;

import static io.dashbase.lexer.state.LexerStates.LexSpace;
import static io.dashbase.lexer.state.LexerStates.LexStatements;
import static io.dashbase.utils.TypeUtils.isSpace;

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
