package io.dashbase.web.utils;

import io.prometheus.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.dashbase.web.utils.States.statementsMap;

public class TextParser {
    private static final Logger logger = LoggerFactory.getLogger(TextParser.class);
    private State textState;
    private boolean error = false;
    private char[] buf = new char[1];
    private String currentToken;
    private Character currentByte;
    private String currentLabel;
    private Map<String, String> currentLabels = new HashMap<>();
    private String currentBucket;
    private String currentQuantile;
    private String currentLabelpair;
    private Map<String, Collector.MetricFamilySamples> summaryMaps;
    private Map<String, Collector.MetricFamilySamples> histogramMaps;

    private Collector.MetricFamilySamples currentFamilySamples = null;
    private List<Histogram> histograms;
    private List<Gauge> gauges;
    private List<Summary> summaries;
    private List<Counter> counters;

    private Histogram histogram;
    private Gauge gauge;
    private Summary summary;
    private Counter counter;

    private Map<String, Collector.MetricFamilySamples> mfs = new HashMap<>();
    private boolean currentIsSummaryCount;
    private boolean currentIsSummarySum;
    private boolean currentIsHistogramCount;
    private boolean currentIsHistogramSum;

    public TextParser() {
    }

    public TextParser(String input) {

    }

    public void skipBlankTab(Reader reader) {
        do {
            currentByte = readChar(reader);
            // TODO
            if (!utils.isBlankOrTab(currentByte)) {
                break;
            }
        } while (true);
    }

    public Map<String, Collector.MetricFamilySamples> run(Reader reader) {
        for (textState = statementsMap.get(TextStates.TextReadNewLine); textState.getTextStates() != TextStates.TextTerminal && textState.getTextStates() != TextStates.TextException; textState = statementsMap.get(textState.nextTo(this, reader))) {
            // magic happens here.
        }

        mfs.entrySet().removeIf(e ->
                e.getValue().samples.size() == 0
        );

        if (error) {
//            a
        }
        return mfs;

    }

    public TextStates visit(TextReadComments textReadComments, Reader reader) {
        skipBlankTab(reader);

        if (this.currentByte == '\n')
            return TextStates.TextReadNewLine;

        readTokenUtilWhiteSpace(reader);

        if (currentByte == '\n')
            return TextStates.TextReadNewLine;

        if (!currentToken.equals("HELP") && !currentToken.equals("TYPE")) {
            do {
                currentByte = readChar(reader);
                if (currentByte == '\n')
                    break;
            } while (true);
            return TextStates.TextReadNewLine;
        }

        skipBlankTab(reader);

        readTokenAsMetricName(reader);
//        this.metricFamily = new Metrics.MetricFamily();
        if (currentByte == '\n') {
            return TextStates.TextReadNewLine;
        }

        if (!utils.isBlankOrTab(currentByte)) {
//            throw new Exception("");
        }


        setOrCreateCurrentMF();
        /**
         * metrics TODO
         */
        skipBlankTab(reader);
        if (currentByte == '\n') {
            return TextStates.TextReadNewLine;
        }
        switch (currentToken) {
            case "HELP":
                return TextStates.TextReadingHelp;
            case "TYPE":
                return TextStates.TextReadingType;
        }

        /*
         should unreachable
         */
        return TextStates.TextException;
    }

    private void setOrCreateCurrentMF() {
        // has the mf
        if (mfs.containsKey(currentToken)) {
            return;
        }
        String summaryName = utils.getSummaryMetricName(currentToken);

        currentFamilySamples = mfs.get(summaryName);
        if (currentFamilySamples != null) {
            if (currentFamilySamples.type == Collector.Type.SUMMARY) {
                if (utils.isCount(currentToken)) {
                    currentIsSummaryCount = true;
                }
                if (utils.isSum(currentToken)) {
                    currentIsSummarySum = true;
                }
                return;
            }
        }

        String histogramName = utils.getHistogramMetricName(currentToken);

        currentFamilySamples = mfs.get(histogramName);
        if (currentFamilySamples != null) {
            if (currentFamilySamples.type == Collector.Type.HISTOGRAM) {
                if (utils.isCount(currentToken)) {
                    currentIsHistogramCount = true;
                }
                if (utils.isSum(currentToken)) {
                    currentIsHistogramSum = true;
                }
                return;
            }
        }
    }


    private void readUtilNewLine(Reader reader) {
        do {
            currentByte = readChar(reader);
            if (currentByte == '\n')
                break;
        } while (true);
    }

    private void readTokenAsMetricName(Reader reader) {
        this.resetCurrentToken();
        if (!utils.isValidMetricNameStart(currentByte)) {
            return;
        }
        do {
            currentToken += currentByte;
            currentByte = readChar(reader);
            if (!utils.isValidMetricNameContinuation(currentByte)) {
                return;
            }
        } while (true);
    }


    private void readTokenUtilWhiteSpace(Reader reader) {
        this.resetCurrentToken();
        while (!utils.isWhiteSpaceOrTabOrNewLine(currentByte)) {
            this.currentToken += currentByte;
            currentByte = readChar(reader);
        }
    }

    public TextStates visit(TextReadNewLine textReadNewLine, Reader reader) {
        skipBlankTab(reader);

        switch (this.currentByte) {
            case '\0':
                return TextStates.TextTerminal;
            case '#':
                return TextStates.TextReadComment;
            case '\n':
                return TextStates.TextReadNewLine;
        }
        return TextStates.TextReadingMetricName;

    }


    private void resetCurrentToken() {
        this.currentToken = "";
    }

    private void appendCurrentToken(Character ch) {
        this.currentToken += ch;
    }

    private char readChar(Reader reader) {
        try {
            reader.mark(1);
            int result = reader.read();
            if (result == -1)
                return '\0';
            return (char) result;
        } catch (IOException e) {
            logger.error("error while processing http response");
        }
        return '\0';
    }

    public TextStates visit(TextStartLabelName textStartLabelName, Reader reader) {
        return TextStates.TextTerminal;

    }

    public TextStates visit(TextStartLabelValue textStartLabelValue, Reader reader) {
        return TextStates.TextTerminal;
    }

    public TextStates visit(TextReadingValue textReadingValue, Reader reader) {
        if (currentFamilySamples.type == Collector.Type.SUMMARY) {

        } else if (currentFamilySamples.type == Collector.Type.HISTOGRAM) {

        } else {
            currentFamilySamples.samples.add(new Collector.MetricFamilySamples.Sample(null, null, null, 0));
        }

        readTokenUtilWhiteSpace(reader);
        return TextStates.TextTerminal;
    }

    public TextStates visit(TextReadingLabel textReadingLabel, Reader reader) {
        if (currentFamilySamples.type == Collector.Type.SUMMARY || currentFamilySamples.type == Collector.Type.HISTOGRAM) {
            currentLabels.clear();
            currentQuantile = "";
            currentBucket = "";
        }
        if (currentByte != '{')
            return TextStates.TextReadingValue;
        return TextStates.TextStartLabelName;

    }

    public TextStates visit(TextReadingMetricName textReadingMetricName, Reader reader) {
        readTokenAsMetricName(reader);
        if (currentToken.length() == 0) {
            throw new TextParseError("invalid metric name");
        }

//        setOrCreateCurrentMF();


        skipBlankTabIfCurrentBlankTab(reader);

        return TextStates.TextReadingLabel;
    }

    private void skipBlankTabIfCurrentBlankTab(Reader reader) {
        if (utils.isBlankOrTab(currentByte)) {
            skipBlankTab(reader);
        }
    }

    public TextStates visit(TextReadingMetricValue textReadingMetricValue, Reader reader) {
        return null;
    }

    public static TextParser parser(String input) {
        return new TextParser(input);
    }

    public List<Collector.MetricFamilySamples> parseExpr(String input) {
        return null;
    }

    public TextStates visit(TextReadingHelp textReadingHelp, Reader reader) {
        readUtilNewLine(reader);
        return TextStates.TextReadNewLine;
    }

    public TextStates visit(TextReadingType textReadingType, Reader reader) {
        readTokenUtilNewLine(reader);
    }

    private void readTokenUtilNewLine(Reader reader) {
        this.resetCurrentToken();
        while (!utils.isWhiteSpaceOrTabOrNewLine(currentByte)) {
            this.currentToken += currentByte;
            currentByte = readChar(reader);
        }
    }
}
