package io.dashbase.parser;

import com.google.common.collect.Lists;
import io.dashbase.exception.ParserException;
import io.dashbase.lexer.QueryLexer;
import io.dashbase.lexer.token.ItemType;
import io.dashbase.lexer.token.TokenItem;
import lombok.NonNull;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.ExprType;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.value.*;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.match.Function;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.utils.Strings;
import io.dashbase.utils.TypeUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static io.dashbase.lexer.token.ItemType.*;
import static io.dashbase.parser.ast.value.ValueType.*;
import static io.dashbase.parser.ast.value.VectorMatching.VectorMatchCardinality.*;
import static io.dashbase.parser.ast.match.Functions.getFunction;
import static io.dashbase.parser.ast.match.Labels.MetricName;
import static io.dashbase.utils.TypeUtils.isLabel;

public final class QueryParser {
    private final QueryLexer lexer;
    private final TokenItem[] tokens;
    private int peekCount;

    private QueryParser(@NonNull String input) {
        this.lexer = QueryLexer.lexer(input);
        this.tokens = new TokenItem[3];
    }

    private TokenItem peek() {
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

    private void backup() {
        this.peekCount++;
    }

    private TokenItem next() {
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

    private void errorf(String format, Object... args) {
//        System.err.printf(format + '\n', args);
        error(format(format, args));
    }

    private void error(String errorMessage) {
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
        }

        if (expr == null) {
            this.errorf("no expression found in input");
        }

        return expr;
    }

    // expr parses any expression.
    private Expr expr() {
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

            // Parse optional operator matching options. Its validity
            // is checked in the type-checking stage.
            VectorMatching vecMatching = VectorMatching.of(CardOneToOne);
            if (op.isSetOperator()) {
                vecMatching.card = CardManyToMany;
            }

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

            if (peek().type == itemOn || peek().type == itemIgnoring) {
                if (peek().type == itemOn) {
                    vecMatching.on = true;
                }

                next();
                vecMatching.matchingLabels = labels();
                // parse grouping

                TokenItem item = peek();
                if (item.type == itemGroupLeft || item.type == itemGroupRight) {
                    next();

                    if (item.type == itemGroupLeft) {
                        vecMatching.card = CardManyToOne;
                    } else {
                        vecMatching.card = CardOneToMany;
                    }

                    if (peek().type == itemLeftParen) {
                        vecMatching.include = labels();
                    }
                }
            }

            for (String matchingLabel : vecMatching.matchingLabels) {
                for (String include : vecMatching.include) {
                    if (matchingLabel.equals(include) && vecMatching.on) {
                        errorf("label \"%s\" must not occur in ON and GROUP clause at once", matchingLabel);
                    }
                }
            }

            Expr rhs = unaryExpr();

            expr = balance(expr, op, rhs, returnBool, vecMatching);
        }
    }

    // labels parses a list of labelnames.
    //
    //		'(' <label_name>, ... ')'
    //
    private List<String> labels() {
        final String context = "grouping opts";
        this.expect(itemLeftParen, context);
        List<String> labels = Lists.newArrayList();
        if (peek().type != itemRightParen) {
            for (; ; ) {
                TokenItem id = next();
                if (!isLabel(id.text)) {
                    errorf("unexpected %s in %s, expected label", id.desc(), context);
                }

                labels.add(id.text);

                if (peek().type != itemComma) {
                    break;
                }

                next();
            }
        }

        expect(itemRightParen, context);
        return labels;
    }

    private BinaryExpr balance(Expr lhs, ItemType op, Expr rhs, boolean returnBool, VectorMatching vectorMatching) {
        if (lhs instanceof BinaryExpr) {
            BinaryExpr lhsBE = (BinaryExpr) lhs;
            int precd = lhsBE.op.precedence() - op.precedence();

            // priority higher or right assoc
            if (precd < 0 || (precd == 0 && op.isRightAssociative())) {
                Expr balanced = balance(lhsBE.rhs, op, rhs, returnBool, vectorMatching);

                if (op.isComparisonOperator() && !returnBool
                        && rhs.valueType() == ValueTypeScalar
                        && lhs.valueType() == ValueTypeScalar) {
                    errorf("comparisons between scalars must use BOOL modifier");
                }

                return BinaryExpr.of(
                        lhsBE.op,
                        lhsBE.lhs,
                        balanced,
                        lhsBE.returnBool,
                        lhsBE.vectorMatching
                );
            }
        }

        if (op.isComparisonOperator() && !returnBool
                && rhs.valueType() == ValueTypeScalar
                && lhs.valueType() == ValueTypeScalar) {
            errorf("comparisons between scalars must use BOOL modifier");
        }

        return BinaryExpr.of(
                op,
                lhs,
                rhs,
                returnBool,
                vectorMatching
        );
    }

    private Expr unaryExpr() {
        TokenItem item = peek();
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

        // Expression might be followed by a range selector.
        if (peek().type == itemLeftBracket) {
            if (Objects.isNull(expr)) {
                errorf("expr should not null");
                return null;
            }

            if (expr.exprType != ExprType.VectorSelector) {
                errorf("range specification must be preceded by a metric selector, but follows a %s instead", expr.toString());
            } else {
                expr = rangeSelector((VectorSelector) expr);
            }
        }

        // Parse optional offset.
        if (peek().type == itemOffset) {
            Duration offset = offset();
            switch (expr.exprType) {
                case VectorSelector: {
                    VectorSelector s = (VectorSelector) expr;
                    s.offset = offset;
                    break;
                }

                case MatrixSelector: {
                    MatrixSelector s = (MatrixSelector) expr;
                    s.offset = offset;
                    break;
                }

                default: {
                    errorf("offset modifier must be preceded by an instant or range selector, but follows a \"%s\" instead", expr.toString());
                }
            }
        }

        return expr;
    }

    // offset parses an offset modifier.
    //
    //		offset <duration>
    //
    private Duration offset() {
        final String context = "offset";
        // consume this token.
        this.next();
        TokenItem offi = expect(itemDuration, context);
        return parseDuration(offi.text);
    }

    private Expr rangeSelector(VectorSelector expr) {
        final String context = "range selector";
        this.next();
        String erangeStr = expect(itemDuration, context).text;
        Duration erange = parseDuration(erangeStr);
        this.expect(itemRightBracket, context);

        return MatrixSelector.of(
                expr.name,
                erange,
                expr.matchers
        );
    }

    private Duration parseDuration(String erangeStr) {
        Duration dur = TypeUtils.parseDuration(erangeStr);
        if (dur.equals(Duration.ZERO)) {
            errorf("duration must be greater than 0");
        }

        return dur;
    }

    // expect consumes the next token and guarantees it has the required type.
    private TokenItem expect(ItemType exp, String context) {
        TokenItem item = next();
        if (item.type != exp) {
            errorf("unexpected %s in %s, expected %s", item.desc(), context, exp.desc());
        }

        return item;
    }

    private Expr primaryExpr() {
        TokenItem item = next();

        if (item.type.isAggregator()) {
            backup();
            return aggrExpr();
        }

        switch (item.type) {
            case itemNumber: {
                double number = parseNumber(item.text);
                return NumberLiteral.of(number);
            }

            case itemString: {
                return StringLiteral.of(Strings.unquote(item.text));
            }

            case itemLeftBrace: {
                backup();
                return VectorSelector("");
            }

            case itemIdentifier:
                if (peek().type == itemLeftParen) {
                    return call(item.text);
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


    // aggrExpr parses an aggregation expression.
    //
    //		<aggr_op> (<Vector_expr>) [by|without <labels>]
    //		<aggr_op> [by|without <labels>] (<Vector_expr>)
    //
    private AggregateExpr aggrExpr() {
        final String ctx = "aggregation";
        TokenItem agop = next();

        if (!agop.type.isAggregator()) {
            errorf("expected aggregation operator but got %s", agop);
        }

        List<String> grouping = Lists.newArrayList();
        boolean without = false;
        boolean modifiersFirst = false;

        ItemType type = peek().type;
        if (type == itemBy || type == itemWithout) {
            if (type == itemWithout) {
                without = true;
            }

            next();
            grouping = labels();
            modifiersFirst = true;
        }

        expect(itemLeftParen, ctx);
        Expr param = null;

        if (agop.type.isAggregatorWithParam()) {
            param = expr();
            expect(itemComma, ctx);
        }

        Expr expr = expr();
        expect(itemRightParen, ctx);

        if (!modifiersFirst) {
            ItemType t = peek().type;
            if (t == itemBy || t == itemWithout) {
                if (!grouping.isEmpty()) {
                    errorf("aggregation must only contain one grouping clause");
                }

                if (t == itemWithout) {
                    without = true;
                }

                next();
                grouping = labels();
            }
        }

        return AggregateExpr.of(
                agop.type,
                expr,
                param,
                grouping,
                without
        );
    }

    // call parses a function call.
    //
    //		<func_name> '(' [ <arg_expr>, ...] ')'
    //
    private Call call(String name) {
        final String ctx = "function call";
        Function function = getFunction(name);
        if (Objects.isNull(function)) {
            errorf("unknown function with name \"%s\"", name);
        }

        expect(itemLeftParen, ctx);
        // Might be call without args.
        if (peek().type == itemRightParen) {
            next(); // consume.
            return Call.of(function, Collections.emptyList());
        }

        List<Expr> args = Lists.newArrayList();
        for (; ; ) {
            Expr e = expr();
            args.add(e);

            // Terminate if no more arguments.
            if (peek().type != itemComma) {
                break;
            }

            next();
        }

        // Call must be closed.
        expect(itemRightParen, ctx);
        return Call.of(function, args);
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
            matchers = labelMatchers(itemEQL, itemNEQ, itemEQLRegex, itemNEQRegex);
        }

        if (!name.equals("")) {
            for (Matcher matcher : matchers) {
                if (matcher.name.equals(MetricName)) {
                    errorf("metric name must not be set twice: \"%s\" or \"%s\"", name, matcher.value);
                }
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

        // A Vector selector must contain at least one non-empty matcher to prevent
        // implicit selection of all metrics (e.g. by a typo).
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

    private List<Matcher> labelMatchers(ItemType... operators) {
        final String ctx = "label matching";
        List<Matcher> matchers = Lists.newArrayList();
        expect(itemLeftBrace, ctx);
        // Check if no matchers are provided.
        if (peek().type == itemRightBrace) {
            next();
            return matchers;
        }


        for (; ; ) {
            TokenItem label = expect(itemIdentifier, ctx);
            ItemType op = next().type;
            if (!op.isOperator()) {
                errorf("expected label matching operator but got %s", op.desc());
            }

            boolean validOp = false;
            for (ItemType allowedOp : operators) {
                if (op == allowedOp) {
                    validOp = true;
                }
            }

            if (!validOp) {
                StringBuilder builder = new StringBuilder();
                for (ItemType operator : operators) {
                    builder.append(operator.desc());
                }

                errorf(
                        "operator must be one of %s, is %s",
                        builder.toString(),
                        op.desc()
                );
            }

            String val = Strings.unquote(expect(itemString, ctx).text);
            // Map the item to the respective match type.
            Matcher.MatchType matchType = null;
            switch (op) {
                case itemEQL:
                    matchType = Matcher.MatchType.MatchEqual;
                    break;
                case itemNEQ:
                    matchType = Matcher.MatchType.MatchNotEqual;
                    break;
                case itemEQLRegex:
                    matchType = Matcher.MatchType.MatchRegexp;
                    break;
                case itemNEQRegex:
                    matchType = Matcher.MatchType.MatchNotRegexp;
                    break;
                default:
                    errorf("item %s is not a metric match type", op.desc());
            }

            Matcher matcher = Matcher.newMatcher(matchType, label.text, val);
            matchers.add(matcher);

            if (peek().type == itemIdentifier) {
                errorf("missing comma before next identifier %s", peek().text);
            }

            // Terminate list if last matcher.
            if (peek().type != itemComma) {
                break;
            }

            next();
            // Allow comma after each item in a multi-line listing.
            if (peek().type == itemRightBrace) {
                break;
            }
        }

        expect(itemRightBrace, ctx);
        return matchers;
    }

    public void typeCheck(Expr expr) {
        checkType(expr);
    }

    // expectType checks the type of the node and raises an error if it
    // is not of the expected type.
    private void expectType(Expr node, ValueType want, String context) {
        ValueType type = checkType(node);

        if (type != want) {
            errorf("expected type %s in %s, got %s", want.documentedType(), context, type.documentedType());
        }
    }

    private ValueType checkType(Expr expr) {
        ValueType valueType;
        switch (expr.exprType) {
            // TODO case Statements, Expressions, Statement
            default: {
                valueType = expr.valueType();
            }
        }

        // Recursively check correct typing for child nodes and raise
        // errors in case of bad typing.
        switch (expr.exprType) {
            case BinaryExpr: {
                BinaryExpr binaryExpr = (BinaryExpr) expr;
                ValueType lhsType = checkType(binaryExpr.lhs);
                ValueType rhsType = checkType(binaryExpr.rhs);

                if (!binaryExpr.op.isOperator()) {
                    errorf("binary expression does not support operator %s", binaryExpr.op.desc());
                }

                if ((lhsType != ValueTypeScalar && lhsType != ValueTypeVector)
                        || (rhsType != ValueTypeScalar && rhsType != ValueTypeVector)) {
                    errorf("binary expression must contain only scalar and instant vector types");
                }

                if ((lhsType != ValueTypeVector || rhsType != ValueTypeVector) && binaryExpr.vectorMatching != null) {
                    if (binaryExpr.vectorMatching.matchingLabels.size() > 0) {
                        errorf("vector matching only allowed between instant vectors");
                    }

                    binaryExpr.vectorMatching = null;
                } else {
                    // Both operands are Vectors.
                    if (binaryExpr.op.isSetOperator() && Objects.nonNull(binaryExpr.vectorMatching)) {
                        if (binaryExpr.vectorMatching.card == CardOneToMany || binaryExpr.vectorMatching.card == CardManyToOne) {
                            errorf("no grouping allowed for \"%s\" operation", binaryExpr.op.desc());
                        }

                        if (binaryExpr.vectorMatching.card != CardManyToMany) {
                            errorf("set operations must always be many-to-many");
                        }
                    }
                }


                if ((lhsType == ValueTypeScalar || rhsType == ValueTypeScalar) && binaryExpr.op.isSetOperator()) {
                    errorf("set operator \"%s\" not allowed in binary scalar expression", binaryExpr.op.desc());
                }

                break;
            }

            case UnaryExpr: {
                UnaryExpr unaryExpr = (UnaryExpr) expr;

                if (unaryExpr.op != itemADD && unaryExpr.op != itemSUB) {
                    errorf("only + and - operators allowed for unary expressions");
                }

                ValueType type = checkType(unaryExpr.expr);
                if (type != ValueTypeScalar && type != ValueTypeVector) {
                    errorf(
                            "unary expression only allowed on expressions of type scalar or instant vector, got \"%s\"",
                            type.documentedType()
                    );
                }
                break;
            }

            case AggregateExpr: {
                AggregateExpr aggregateExpr = (AggregateExpr) expr;
                if (!aggregateExpr.op.isAggregator()) {
                    errorf("aggregation operator expected in aggregation expression but got %s", aggregateExpr.op.desc());
                }

                expectType(aggregateExpr.expr, ValueTypeVector, "aggregation expression");

                ItemType op = aggregateExpr.op;
                if (op == itemTopK || op == itemBottomK || op == itemQuantile) {
                    expectType(aggregateExpr.param, ValueTypeScalar, "aggregation parameter");
                }

                if (op == itemCountValues) {
                    expectType(aggregateExpr.param, ValueTypeString, "aggregation parameter");
                }

                break;
            }

            case Call: {
                Call call = (Call) expr;
                int nargs = call.function.argsTypes.size();

                if (call.function.variadic == 0 && nargs != call.args.size()) {
                    errorf("expected %d argument(s) in call to \"%s\", got %d", nargs, call.function.name, call.args.size());
                } else {
                    int na = nargs - 1;
                    if (na > call.args.size()) {
                        errorf("expected at least %d argument(s) in call to \"%s\", got %d", na, call.function.name, call.args.size());
                    }

                    int nargsMax = na + call.function.variadic;
                    if (call.function.variadic > 0 && nargsMax < call.args.size()) {
                        errorf("expected at most %d argument(s) in call to \"%s\", got %d", nargsMax, call.function.name, call.args.size());
                    }

                    int argTypesLen = call.function.argsTypes.size();
                    for (int i = 0; i < call.args.size(); i++) {
                        Expr arg = call.args.get(i);

                        if (i >= argTypesLen) {
                            i = argTypesLen - 1;
                        }

                        expectType(arg, call.function.argsTypes.get(i), format("call to function \"%s\"", call.function.name));
                    }
                }

                break;
            }

            case ParenExpr: {
                ParenExpr parenExpr = (ParenExpr) expr;
                checkType(parenExpr.inner);
                break;
            }

            case NumberLiteral:
            case MatrixSelector:
            case StringLiteral:
            case VectorSelector: {
                // Nothing to do for terminals.
                break;
            }

            //  default: {
            //      errorf("unknown node type: %s", expr.toString());
            //  }
        }

        return valueType;
    }

    public static QueryParser parser(String input) {
        return new QueryParser(input);
    }

    public static Expr parseExpr(String input) {
        QueryParser parser = parser(input);
        Expr expr = parser.parserExpr();
        parser.typeCheck(expr);
        return expr;
    }
}
