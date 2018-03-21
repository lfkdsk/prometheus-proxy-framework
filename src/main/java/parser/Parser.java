package parser;

import com.google.common.collect.Lists;
import exception.ParserException;
import lexer.QueryLexer;
import lexer.token.ItemType;
import lexer.token.TokenItem;
import lombok.NonNull;
import parser.ast.Expr;
import parser.ast.expr.BinaryExpr;
import parser.ast.expr.ParenExpr;
import parser.ast.expr.UnaryExpr;
import parser.ast.literal.NumberLiteral;
import parser.ast.value.VectorSelector;
import parser.match.Matcher;

import java.util.List;

import static java.lang.String.format;
import static lexer.token.ItemType.*;
import static parser.match.Labels.MetricName;

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

            if (expr != null) {
                this.errorf("could not parse remaining input \"%s\"...", lexer.fromLastPosition());
            }

            expr = expr();

//            if (Objects.nonNull(expr)) {
//                break;
//            }
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
            if (!op.isOperator()) {
                return expr;
            }

            // consume op
            next();

            // TODO multi types
            // TODO vector matching

            boolean returnBool = false;
            // Parse bool modifier.
            if (peek().type == itemBool) {
                if (!op.isComparisonOperator()) {
                    errorf("bool modifier can only be used on comparison operators");
                }

                // consume
                next();
                returnBool = true;
            }

            Expr rhs = unaryExpr();

            expr = balance(expr, op, rhs, returnBool);
        }
    }

    private BinaryExpr balance(Expr lhs, ItemType op, Expr rhs, boolean returnBool) {
        if (lhs instanceof BinaryExpr) {
            BinaryExpr lhsBE = (BinaryExpr) lhs;
            int precd = lhsBE.op.precedence() - op.precedence();

            // priority higher or right assoc
            if (precd < 0 || (precd == 0 && op.isRightAssociative())) {
                Expr balanced = balance(lhsBE.rhs, op, rhs, returnBool);

                //                if lhsBE.Op.isComparisonOperator() && !lhsBE.ReturnBool && balanced.Type() == ValueTypeScalar && lhsBE.LHS.Type() == ValueTypeScalar {
                //                    p.errorf("comparisons between scalars must use BOOL modifier")
                //                }

                if (lhsBE.op.isComparisonOperator() && false
                    // TODO about scalar
                        ) {
                    errorf("comparisons between scalars must use BOOL modifier");
                }

                return BinaryExpr.of(
                        lhsBE.op,
                        lhsBE.lhs,
                        balanced,
                        lhsBE.returnBool
                );
            }
        }

        return BinaryExpr.of(
                op,
                lhs,
                rhs,
                returnBool
        );
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

                return UnaryExpr.of(item.type, next);
            }

            case itemLeftParen: {
                next();

                Expr e = expr();
                expect(itemRightParen, "paren expression");
                return ParenExpr.of(e);
            }
        }

        Expr expr = primaryExpr();
        // TODO last parser

        // Expression might be followed by a range selector.
        if (peek().type == itemLeftBracket) {

        }
        return expr;
    }

    // expect consumes the next token and guarantees it has the required type.
    public TokenItem expect(ItemType exp, String context) {
        TokenItem item = next();
        if (item.type != exp) {
            errorf("unexpected %s in %s, expected %s", item.desc(), context, exp.desc());
        }

        return item;
    }

    private Expr primaryExpr() {
        TokenItem item = next();
        switch (item.type) {
            case itemNumber: {
                double number = parseNumber(item.text);
                return NumberLiteral.of(number);
            }

            case itemLeftBrace: {
                backup();
                return VectorSelector("");
            }

            case itemIdentifier:
                if (peek().type == itemLeftParen) {
                    // TODO call()
                }
            case itemMetricIdentifier:
                return VectorSelector(item.text);
            default: {
                errorf("no valid expression found");
                break;
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
        } else if (text.startsWith("0")) {
            return Integer.valueOf(text.substring(1), 8);
        }

        try {
            return Double.valueOf(text);
        } catch (Exception e) {
            this.errorf("error parsing number: %s", e.getMessage());
        }

        // useless
        return -1;
    }

    // VectorSelector parses a new (instant) vector selector.
    //
    //		<metric_identifier> [<label_matchers>]
    //		[<metric_identifier>] <label_matchers>
    private VectorSelector VectorSelector(String name) {
        List<Matcher> matchers = Lists.newArrayList();
        if (peek().type == itemLeftBrace) {
            // TODO
        }

        // Metric name must not be set in the label matchers and before at the same time.
        //        if name != "" {
        //            for _, m := range matchers {
        //                if m.Name == labels.MetricName {
        //                    p.errorf("metric name must not be set twice: %q or %q", name, m.Value)
        //                }
        //            }
        //            // Set name label matching.
        //            m, err := labels.NewMatcher(labels.MatchEqual, labels.MetricName, name)
        //            if err != nil {
        //                panic(err) // Must not happen with metric.Equal.
        //            }
        //            matchers = append(matchers, m)
        //        }

        if (!name.equals("")) {
            for (Matcher matcher : matchers) {
                // todo
            }

            // Set name label matching.
            Matcher matcher = Matcher.newMatcher(
                    Matcher.MatchType.MatchEqual,
                    MetricName,
                    name
            );

            matchers.add(matcher);
        }

        if (matchers.size() == 0) {
            errorf("vector selector must contain label matchers or metric name");
        }

        boolean notEmpty = false;
        for (Matcher matcher : matchers) {
            if (!matcher.matches("")) {
                notEmpty = true;
                break;
            }
        }

        if (!notEmpty) {
            errorf("vector selector must contain at least one non-empty matcher");
        }

        return VectorSelector.of(
                name,
                matchers
        );
    }

    public void typeCheck(Expr expr) {
        checkType(expr);
    }

    public void checkType(Expr expr) {
        switch (expr.exprType) {
            // TODO case Statements, Expressions, Statement
            case ParenExpr:
        }
    }

    public static Parser parser(String input) {
        return new Parser(input);
    }
}
