package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;
import io.dashbase.lexer.token.ItemType;

import java.util.Objects;

import static io.dashbase.lexer.state.States.statementsMap;

@StatesBinder(binder = LexerStates.LexString)
public class LexString extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        for (; ; ) {
            Character ch = lexer.next();
            if (Objects.isNull(ch) || Objects.equals(ch, '\n')) {
                return lexer.error("unterminated quoted string");
            }

            if (Objects.equals(ch, '\\')) {
                statementsMap.get(LexerStates.LexEscape)
                             .nextTo(lexer);
            } else if (Objects.equals(ch, lexer.getStringOpen())) {
                break;
            }
        }

        lexer.emit(ItemType.itemString);
        return LexerStates.LexStatements;
    }
}
