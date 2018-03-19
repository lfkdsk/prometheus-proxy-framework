package lexer.state;

import com.google.common.collect.Maps;
import token.ItemType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class States {

    public static final Map<LexerStates, State> statementsMap = Maps.newEnumMap(LexerStates.class);

    public static final Map<String, ItemType> keywordsMap = Maps.newHashMap();

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
                new LexRawString()
        ).forEach(state -> statementsMap.put(state.getLexerStates(), state));

        // initial keyword map
        Stream.of(ItemType.values())
              .filter(ItemType::isKeyword)
              .filter(itemType -> Objects.nonNull(itemType.getKey()))
              .forEach(itemType -> keywordsMap.put(itemType.getKey(), itemType));

        keywordsMap.put("nan", ItemType.itemNumber);
        keywordsMap.put("inf", ItemType.itemNumber);
    }
}
