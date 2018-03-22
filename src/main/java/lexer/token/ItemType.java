package lexer.token;

import lombok.Getter;

public enum ItemType {
    itemError,
    itemEOF,
    itemComment,
    itemIdentifier,
    itemMetricIdentifier,
    itemLeftParen(false, "("),
    itemRightParen(false, ")"),
    itemLeftBrace(false, "{"),
    itemRightBrace(false, "}"),
    itemLeftBracket(false, "]"),
    itemRightBracket(false, "["),
    itemComma(false, ","),
    itemAssign(false, "="),
    itemSemicolon(false, ";"),
    itemString,
    itemNumber,
    itemDuration,
    itemBlank(false, "_"),
    itemTimes(false, "x"),

    // Operators.
    operatorsStart,
    itemSUB(false, "-"),
    itemADD(false, "+"),
    itemMUL(false, "*"),
    itemMOD(false, "%"),
    itemDIV(false, "/"),
    itemLAND("and"),
    itemLOR("or"),
    itemLUnless("unless"),
    itemEQL(false, "=="),
    itemNEQ(false, "!="),
    itemLTE(false, "<="),
    itemLSS(false, "<"),
    itemGTE(false, ">="),
    itemGTR(false, ">"),
    itemEQLRegex(false, "=~"),
    itemNEQRegex(false, "!~"),
    itemPOW(false, "^"),
    operatorsEnd,

    // Aggregators.
    aggregatorsStart,
    itemAvg("avg"),
    itemCount("count"),
    itemSum("sum"),
    itemMin("min"),
    itemMax("max"),
    itemStddev("stddev"),
    itemStdvar("stdvar"),
    itemTopK("topk"),
    itemBottomK("bottomk"),
    itemCountValues("count_values"),
    itemQuantile("quantile"),
    aggregatorsEnd,

    // Keywords.
    keywordsStart,
    itemAlert("alert"),
    itemIf("if"),
    itemFor("for"),
    itemLabels("labels"),
    itemAnnotations("annotations"),
    itemOffset("offset"),
    itemBy("by"),
    itemWithout("without"),
    itemOn("on"),
    itemIgnoring("ignoring"),
    itemGroupLeft("group_left"),
    itemGroupRight("group_right"),
    itemBool("bool"),
    keywordsEnd;

    @Getter
    private String key;
    @Getter
    private String text;
    @Getter
    private boolean isKeyword;

    ItemType() { }

    ItemType(String text) {
        this.key = text;
        this.isKeyword = true;
    }

    ItemType(boolean isKeyword, String text) {
        this.isKeyword = isKeyword;
        if (isKeyword) {
            this.key = text;
        } else {
            this.text = text;
        }
    }

    public boolean isOperator() {
        return this.compareTo(operatorsStart) > 0 && this.compareTo(operatorsEnd) < 0;
    }

    // LowestPrec is a constant for operator precedence in expressions.
    private final static int LowestPrec = 0; // Non-operators.

    // Precedence returns the operator precedence of the binary
    // operator op. If op is not a binary operator, the result
    // is LowestPrec.
    public int precedence() {
        switch (this) {
            case itemLOR: {
                return 1;
            }

            case itemLAND:
            case itemLUnless: {
                return 2;
            }

            case itemEQL:
            case itemNEQ:
            case itemLTE:
            case itemLSS:
            case itemGTE:
            case itemGTR: {
                return 3;
            }

            case itemADD:
            case itemSUB: {
                return 4;
            }

            case itemMUL:
            case itemDIV:
            case itemMOD: {
                return 5;
            }

            case itemPOW: {
                return 6;
            }
            default:
                return LowestPrec;
        }
    }

    // isCompairsonOperator returns true if the item corresponds to a comparison operator.
    // Returns false otherwise.
    public boolean isComparisonOperator() {
        switch (this) {
            case itemEQL:
            case itemNEQ:
            case itemLTE:
            case itemLSS:
            case itemGTE:
            case itemGTR:
                return true;
            default:
                return false;
        }
    }

    public boolean isRightAssociative() {
        switch (this) {
            case itemPOW:
                return true;
            default:
                return false;
        }
    }

    public boolean isAggregator() {
        return this.compareTo(aggregatorsStart) > 0 && this.compareTo(aggregatorsEnd) < 0;
    }

    // isAggregator returns true if the item is an aggregator that takes a parameter.
    // Returns false otherwise
    public boolean isAggregatorWithParam() {
        return this == itemTopK || this == itemBottomK || this == itemCountValues || this == itemQuantile;
    }

    public String desc() {
        switch (this) {
            case itemError:
                return "Error";
            case itemEOF:
                return "end of input";
            case itemComment:
                return "comment";
            case itemIdentifier:
                return "identifier";
            case itemMetricIdentifier:
                return "metric identifier";
            case itemString:
                return "string";
            case itemNumber:
                return "number";
            case itemDuration:
                return "duration";
        }

        return isKeyword ? key : text;
    }

    // isSetOperator returns whether the item corresponds to a set operator.
    public boolean isSetOperator() {
        switch (this) {
            case itemLAND:
            case itemLOR:
            case itemLUnless:
                return true;
        }
        return false;
    }
}

