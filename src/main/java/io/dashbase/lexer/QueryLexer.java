package io.dashbase.lexer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.dashbase.lexer.state.LexerStates;
import io.dashbase.lexer.state.State;
import lombok.Getter;
import lombok.Setter;
import io.dashbase.lexer.token.ItemType;
import io.dashbase.lexer.token.TokenItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static io.dashbase.lexer.state.LexerStates.LexStatements;
import static io.dashbase.lexer.state.LexerStates.LexTerminal;
import static io.dashbase.lexer.state.States.statementsMap;
import static io.dashbase.utils.TypeUtils.count;
import static io.dashbase.utils.TypeUtils.isAlphaNumeric;

public final class QueryLexer {
    @Getter
    private final String input;
    private State state;

    @Getter
    @Setter
    private int position;

    private int start;

    private int lastPostion;

    @Getter
    @Setter
    private int parenDepth;  // Nesting depth of ( ) exprs.

    @Getter
    @Setter
    private boolean braceOpen; // Whether a { is opened.

    @Getter
    @Setter
    private boolean bracketOpen; // Whether a [ is opened.

    @Setter
    @Getter
    private char stringOpen;

    // seriesDesc is set when a series description for the testing
    // language is lexed.
    @Getter
    @Setter
    private boolean seriesDesc;

    private Character currentChar;

    @Getter
    private LinkedList<TokenItem> items;

    public QueryLexer(String input) {
        this.input = input;
        this.items = Lists.newLinkedList();
        this.state = statementsMap.get(LexStatements);
    }

    public Character next() {
        if (position >= input.length()) {
            this.currentChar = null;
            return null;
        }

        char subStr = currentChar = input.charAt(position);
        this.position += 1;
        return subStr;
    }

    public Character peek() {
        if (position >= input.length()) {
            return null;
        }

        Character c = next();
        backup();
        return c;
    }

    public void backup() {
        if (Objects.nonNull(currentChar)) {
            this.position -= 1;
        }
    }

    public void ignore() {
        this.start = this.position;
    }

    public void emit(ItemType itemType) {
        this.items.add(TokenItem.of(itemType, start, input.substring(start, position)));
        // convert position
        this.start = this.position;
    }

    public boolean accept(String valid) {
        if (valid.contains(String.valueOf(next()))) {
            return true;
        }

        this.backup();
        return false;
    }

    public void acceptRun(String valid) {
        for (; valid.contains(String.valueOf(next())); ) ;

        this.backup();
    }

    public String left() {
        return input.substring(this.position);
    }

    public String current() {
        return input.substring(this.start, this.position);
    }

    public String fromLastPosition() {
        return input.substring(this.lastPostion);
    }

    public boolean scanNumber() {
        String digits = "0123456789";
        if (!this.isSeriesDesc() && this.accept("0") && this.accept("xX")) {
            // multi-type number
            digits = "0123456789abcdefABCDEF";
        }

        this.acceptRun(digits);
        if (this.accept(".")) {
            this.acceptRun(digits);
        }

        // science-number
        if (this.accept("eE")) {
            this.accept("+-");
            this.acceptRun("0123456789");
        }

        Character ch = peek();
        if (Objects.isNull(ch)) {
            return true;
        }

        // next char in world
        if ((this.isSeriesDesc() && ch == 'x') || !isAlphaNumeric(ch)) {
            return true;
        }

        return false;
    }

    public LexerStates error(String format, Object... args) {
//        System.err.printf(format + '\n', args);
        this.items.add(TokenItem.of(ItemType.itemError, start, format(format, args)));
        return LexTerminal;
    }

    public TokenItem nextItem() {
        TokenItem item = items.pollFirst();
        // next pos
        this.lastPostion = item.position;
        return item;
    }

    public int lineNumber() {
        return (int) (1 + count(input.substring(0 , lastPostion), '\n'));
    }

    public QueryLexer run() {
        for (state = statementsMap.get(LexStatements); state.getLexerStates() != LexTerminal; ) {
            state = statementsMap.get(state.nextTo(this));
        }

        return this;
    }

    public static QueryLexer lexer(String input) {
        return new QueryLexer(input).run();
    }
}
