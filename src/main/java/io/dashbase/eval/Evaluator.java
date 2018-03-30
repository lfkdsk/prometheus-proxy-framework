package io.dashbase.eval;

import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.value.Values;
import org.jetbrains.annotations.NotNull;
import rapid.api.query.Query;

public final class Evaluator {

    public static Expr expr(@NotNull String query) {
        return Parser.parseExpr(query);
    }

    public static Values.Value eval(@NotNull Expr expr, long timestamp) {
        EvalVisitor visitor = new EvalVisitor(timestamp);
        return expr.accept(visitor);
    }

    public static Query query(@NotNull Expr expr) {
        QueryEvalVisitor visitor = new QueryEvalVisitor();
        return expr.accept(visitor);
    }
}
