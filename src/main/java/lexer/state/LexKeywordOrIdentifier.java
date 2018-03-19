package lexer.state;

import lexer.Lexer;
import token.ItemType;

import java.util.Objects;

import static lexer.state.LexerStates.*;
import static lexer.state.States.keywordsMap;
import static utils.NumberUtils.isAlphaNumeric;

@StatesBinder(binder = LexKeywordOrIdentifier)
public class LexKeywordOrIdentifier extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
        for (; ; ) {
            Character ch = lexer.next();
            if (Objects.nonNull(ch) && (isAlphaNumeric(ch) || ch == ':')) {
                continue;
            }

            // nonNull not backup
            if (Objects.nonNull(ch)) {
                lexer.backup();
            }

            String word = lexer.current();
            ItemType type = keywordsMap.get(word.toLowerCase());
            if (Objects.nonNull(type)) {
                lexer.emit(type);
            } else if (!word.contains(":")) {
                lexer.emit(ItemType.itemIdentifier);
            } else {
                lexer.emit(ItemType.itemMetricIdentifier);
            }
            break;
        }

        if (lexer.isSeriesDesc() && Objects.nonNull(lexer.peek()) && lexer.peek() != '{') {
            return LexValueSequence;
        }

        return LexStatements;
    }
}
