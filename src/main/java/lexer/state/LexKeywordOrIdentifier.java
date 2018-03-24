package lexer.state;

import lexer.QueryLexer;
import lexer.token.ItemType;

import java.util.Objects;

import static lexer.state.LexerStates.*;
import static lexer.state.States.keywordsMap;
import static utils.TypeUtils.isAlphaNumeric;

@StatesBinder(binder = LexKeywordOrIdentifier)
public class LexKeywordOrIdentifier extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
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

        Character ch = lexer.peek();
        if (lexer.isSeriesDesc() && Objects.nonNull(ch) && ch != '{') {
            return LexValueSequence;
        }

        return LexStatements;
    }
}
