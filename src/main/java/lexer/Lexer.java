package lexer;

import com.google.common.collect.Lists;
import lexer.state.State;
import lombok.Getter;
import lombok.Setter;
import token.ItemType;
import token.TokenItem;

import java.util.List;

import static lexer.state.LexerStates.LexStatements;
import static lexer.state.States.statementsMap;
import static utils.NumberUtils.isAlphaNumeric;

public class Lexer {
    @Getter
    private final String input;
    private State state;
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

    @Getter
    private List<TokenItem> items;

    public Lexer(String input) {
        this.input = input;
        this.items = Lists.newLinkedList();
        this.state = statementsMap.get(LexStatements);
    }

    public Character next() {
        if (position >= input.length()) {
            return null;
        }

        char subStr = input.charAt(position);
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
        this.position -= 1;
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
        for (; valid.contains(String.valueOf(next())); ) {/* consume next value*/}
        this.backup();
    }

    public String left() {
        return input.substring(this.position);
    }

    public String current() {
        return input.substring(this.start, this.position);
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

        Character ch = this.peek();
        if ((this.isSeriesDesc() && ch == 'x') || !isAlphaNumeric(ch)) {
            return true;
        }

        return false;
    }

    public void run() {
        for (state = statementsMap.get(LexStatements); state != null; ) {
            state = statementsMap.get(state.nextTo(this));
        }
    }
}
