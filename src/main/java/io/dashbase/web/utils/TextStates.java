package io.dashbase.web.utils;

public enum TextStates {
    TextTerminal,
    TextException,
    TextStartLabelName,
    TextStartLabelValue,
    TextReadingLabel,
    TextReadingValue,
    TextReadNewLine,
    TextReadComment,
    TextReadingType,
    TextReadingHelp,
    TextReadingMetricName,
    TextReadingMetricValue,
}
