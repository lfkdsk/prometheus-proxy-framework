package io.dashbase.eval;

import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;
import org.jetbrains.annotations.NotNull;
import rapid.api.query.Query;

public final class Evaluator {
    public static final QueryExprVisitor visitor = new QueryExprVisitor();

    public static Expr expr(@NotNull String query) {
        return Parser.parseExpr(query);
    }

    public static Query eval(@NotNull String query) {
        Expr expr = expr(query);
        return expr.accept(visitor);
    }
}
