package token;

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
}
