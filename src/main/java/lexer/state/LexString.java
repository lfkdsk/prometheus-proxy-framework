package lexer.state;

import lexer.Lexer;
import token.ItemType;

import java.util.Objects;

import static lexer.state.States.statementsMap;

@StatesBinder(binder = LexerStates.LexString)
public class LexString extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
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
