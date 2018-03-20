package parser;

import exception.ParserException;
import lexer.QueryLexer;
import lexer.token.ItemType;
import lexer.token.TokenItem;
import lombok.NonNull;
import parser.ast.Expr;
import parser.ast.literal.NumberLiteral;

import java.util.Objects;

import static java.lang.String.format;
import static lexer.token.ItemType.*;

public final class Parser {
    private final QueryLexer lexer;
    private final TokenItem[] tokens;
    private int peekCount;

    public Parser(@NonNull String input) {
        this.lexer = QueryLexer.lexer(input);
        this.tokens = new TokenItem[3];
    }

    public TokenItem peek() {
        if (peekCount > 0) {
            return tokens[peekCount - 1];
        }

        peekCount = 1;
        TokenItem item = lexer.nextItem();
        // Skip comments
        for (; item.type == itemComment; ) {
            item = lexer.nextItem();
        }

        tokens[0] = item;
        return tokens[0];
    }

    public void backup() {
        this.peekCount++;
    }

    public TokenItem next() {
        if (peekCount > 0) {
            this.peekCount--;
        } else {
            // skip comments
            TokenItem item = lexer.nextItem();
            for (; item.type == itemComment; ) {
                item = lexer.nextItem();
            }

            tokens[0] = item;
        }

        if (tokens[peekCount].type == itemError) {
            this.errorf("%s", tokens[peekCount].text);
        }

        return tokens[peekCount];
    }

    public void errorf(String format, Object... args) {
        System.err.printf(format, args);
        error(format(format, args));
    }

    public void error(String errorMessage) {
        throw new ParserException(lexer.lineNumber(), lexer.getPosition(), errorMessage);
    }

    public Expr parserExpr() {
        Expr expr = null;
        for (; peek().type != itemEOF; ) {
            if (peek().type == itemComment) {
                continue;
            }

            //            if (expr != null) {
            //                this.errorf("could not parse remaining input $.15s...", lexer.fromLastPosition());
            //            }

            expr = expr();

            if (Objects.nonNull(expr)) {
                break;
            }
        }

        if (expr == null) {
            this.errorf("no expression found in input");
        }

        return expr;
    }

    // expr parses any expression.
    public Expr expr() {
        // Parse the starting expression.
        Expr expr = unaryExpr();
        // Loop through the operations and construct a binary operation tree based
        // on the operators' precedence.
        for (; ; ) {
            ItemType op = peek().type;
            if (!isOperator(op)) {
                return expr;
            }

            // consume op
            next();

            // TODO multi types
        }
    }

    private Expr unaryExpr() {
        TokenItem item = peek();
        // TODO + - [
        switch (item.type) {
            case itemADD:
            case itemSUB: {
                next();
                // consume this -/+
                Expr next = unaryExpr();

                if (next instanceof NumberLiteral) {
                    if (item.type == itemSUB) {
                        ((NumberLiteral) next).number *= -1;
                    }

                    return next;
                }
            }
        }

        Expr expr = primaryExpr();
        // TODO last parser
        return expr;
    }

    private Expr primaryExpr() {
        TokenItem item = next();
        switch (item.type) {
            case itemNumber: {
                double number = parseNumber(item.text);
                return NumberLiteral.of(number);
            }
        }

        return null;
    }

    private double parseNumber(String text) {
        // inf parser
        if (text.toLowerCase().contains("inf")) {
            return Double.POSITIVE_INFINITY;
        } else if (text.toLowerCase().startsWith("0x")) {
            return Integer.valueOf(text.substring(2), 16);
        }

        try {
            return Double.valueOf(text);
        } catch (Exception e) {
            this.errorf("error parsing number: %s", e.getMessage());
        }

        // useless
        return -1;
    }

    public static Parser parser(String input) {
        return new Parser(input);
    }
}
