package io.dashbase.web.utils;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Stream;

public abstract class States {


    public static final Map<TextStates, State> statementsMap = Maps.newEnumMap(TextStates.class);

    static {
        // initial statements
        Stream.of(
                new TextReadingLabel(),
                new TextStartLabelName(),
                new TextStartLabelValue(),
                new TextReadingValue(),
                new TextReadNewLine(),
                new TextReadComments(),
                new TextReadingMetricName(),
                new TextReadingType(),
                new TextReadingHelp(),
                new TextReadingMetricValue(),
                new TextTerminal()
        ).forEach(state -> statementsMap.put(state.getTextStates(), state));

    }
}
