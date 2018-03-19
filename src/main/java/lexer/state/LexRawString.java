package lexer.state;

import lexer.Lexer;
import token.ItemType;

import java.util.Objects;

@StatesBinder(binder = LexerStates.LexRawString)
public class LexRawString extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {

        for (; ; ) {
            Character ch = lexer.next();
            if (Objects.isNull(ch)) {
                return lexer.error("unterminated raw string");
            }

            if (Objects.equals(ch, lexer.getStringOpen())) {
                break;
            }
        }

        lexer.emit(ItemType.itemString);
        return LexerStates.LexStatements;
    }
}
