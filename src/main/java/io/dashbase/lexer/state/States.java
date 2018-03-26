package io.dashbase.lexer.state;

import com.google.common.collect.Maps;
import io.dashbase.lexer.QueryLexer;
import io.dashbase.lexer.token.ItemType;

import java.util.*;
import java.util.stream.Stream;

public final class States {

    public static final Map<LexerStates, State> statementsMap = Maps.newEnumMap(LexerStates.class);

    public static final Map<String, ItemType> keywordsMap = Maps.newHashMap();

    public static final Map<ItemType, String> itemTypeStr = Maps.newHashMap();

    @StatesBinder(binder = LexerStates.LexTerminal)
    private static class LexTerminal extends State {

        @Override
        public LexerStates nextTo(QueryLexer lexer) {
            return LexerStates.LexTerminal;
        }
    }

    static {
        // initial statements
        Stream.of(
                new LexStatements(),
                new LexSpace(),
                new LexInsideBrace(),
                new LexDuration(),
                new LexNumberOrDuration(),
                new LexKeywordOrIdentifier(),
                new LexString(),
                new LexEscape(),
                new LexRawString(),
                new LexLineComment(),
                new LexIdentifier(),
                new LexValueSequence(),
                new LexNumber(),
                new LexTerminal()
        ).forEach(state -> statementsMap.put(state.getLexerStates(), state));

        // initial keyword map
        Stream.of(ItemType.values())
              .filter(ItemType::isKeyword)
              .filter(itemType -> Objects.nonNull(itemType.getKey()))
              .forEach(itemType -> keywordsMap.put(itemType.getKey(), itemType));

        keywordsMap.put("nan", ItemType.itemNumber);
        keywordsMap.put("inf", ItemType.itemNumber);

        Stream.of(ItemType.values())
              .filter(itemType -> !itemType.isKeyword())
              .filter(itemType -> Objects.nonNull(itemType.getText()))
              .forEach(itemType -> itemTypeStr.put(itemType, itemType.getText()));
    }
}
