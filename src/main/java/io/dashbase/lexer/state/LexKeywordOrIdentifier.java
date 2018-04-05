package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;
import io.dashbase.lexer.token.ItemType;

import java.util.Objects;

import static io.dashbase.lexer.state.LexerStates.*;
import static io.dashbase.lexer.state.States.keywordsMap;
import static io.dashbase.utils.TypeUtils.isAlphaNumeric;
import static io.dashbase.utils.TypeUtils.isKeyWordOrIdentifier;

@StatesBinder(binder = LexKeywordOrIdentifier)
public class LexKeywordOrIdentifier extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        for (; ; ) {
            Character ch = lexer.next();
            if (Objects.nonNull(ch) && (isKeyWordOrIdentifier(ch) || ch == ':')) {
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
