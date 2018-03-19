package lexer.state;

import com.google.common.collect.Maps;

import java.util.EnumMap;
import java.util.stream.Stream;

public final class States {

    public static final EnumMap<LexerStates, State> statementsMap = Maps.newEnumMap(LexerStates.class);

    static {
        Stream.of(
                new LexStatements(),
                new LexSpace(),
                new LexInsideBrace(),
                new LexDuration()
        ).forEach(state -> statementsMap.put(state.getLexerStates(), state));
    }
}
