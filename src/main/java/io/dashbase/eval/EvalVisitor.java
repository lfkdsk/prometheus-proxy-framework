package io.dashbase.eval;

import io.dashbase.eval.binder.ExprVisitor;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.Values;
import io.dashbase.parser.ast.value.VectorSelector;
import io.dashbase.web.converter.ResponseFactory;

public final class EvalVisitor implements ExprVisitor<Values.Value> {

    private ResponseFactory factory;

    public EvalVisitor(ResponseFactory factory) {
        this.factory = factory;
    }

    @Override
    public Values.Value visit(AggregateExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(MatrixSelector visitor) {
        return null;
    }

    @Override
    public Values.Value visit(VectorSelector visitor) {
        // TODO: need result value to get more specs
//        Vector vector = Vector.of();
//        List<Vector.Sample> samples = vector.getSamples();
//
//        for (Matcher matcher : visitor.matchers) {
//            Vector.Sample sample = new Vector.Sample();
//            Map<String, String> metric = sample.getMetric();
//            metric.put(matcher.name, matcher.value);
//            samples.add(sample);
//        }

        return null;
    }

    @Override
    public Values.Value visit(BinaryExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(ParenExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(UnaryExpr visitor) {
        return null;
    }

    @Override
    public Values.Value visit(NumberLiteral visitor) {
        return Values.Scalar.of(factory.getStart(), String.valueOf(visitor.number));
    }

    @Override
    public Values.Value visit(StringLiteral visitor) {
        return null;
    }

    @Override
    public Values.Value visit(Call visitor) {
        return null;
    }
}
