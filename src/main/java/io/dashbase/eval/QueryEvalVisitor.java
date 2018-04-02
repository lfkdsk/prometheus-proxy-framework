package io.dashbase.eval;

import com.google.common.collect.Lists;
import io.dashbase.lexer.token.ItemType;
import io.dashbase.parser.ast.Expr;
import io.dashbase.parser.ast.ExprType;
import io.dashbase.parser.ast.expr.BinaryExpr;
import io.dashbase.parser.ast.expr.ParenExpr;
import io.dashbase.parser.ast.expr.UnaryExpr;
import io.dashbase.parser.ast.literal.NumberLiteral;
import io.dashbase.parser.ast.literal.StringLiteral;
import io.dashbase.parser.ast.match.Call;
import io.dashbase.parser.ast.match.Function;
import io.dashbase.parser.ast.match.Functions;
import io.dashbase.parser.ast.match.Matcher;
import io.dashbase.parser.ast.value.AggregateExpr;
import io.dashbase.parser.ast.value.MatrixSelector;
import io.dashbase.parser.ast.value.ValueType;
import io.dashbase.parser.ast.value.VectorSelector;
import io.dashbase.web.converter.ResponseFactory;
import rapid.api.AggregationRequest;
import rapid.api.RapidRequest;
import rapid.api.query.Conjunction;
import rapid.api.query.EqualityQuery;
import rapid.api.query.Query;
import rapid.api.query.StringQuery;

import java.util.List;

import static io.dashbase.parser.ast.literal.NumberLiteral.trimZeroOfNumber;
import static java.lang.String.format;

public final class QueryEvalVisitor implements ExprVisitor<Query> {

    private ResponseFactory factory;

    public QueryEvalVisitor() {

    }

    public QueryEvalVisitor(ResponseFactory factory) {
        this.factory = factory;
    }

    @Override
    public Query visit(AggregateExpr visitor) {
        return null;
    }

    @Override
    public Query visit(MatrixSelector visitor) {
        return null;
    }

    @Override
    public Query visit(VectorSelector visitor) {
        List<Query> queries = Lists.newArrayList();

        for (Matcher matcher : visitor.matchers) {
            switch (matcher.type) {
                case MatchEqual: {
                    EqualityQuery query = new EqualityQuery(matcher.name, matcher.value);
                    queries.add(query);
                    break;
                }

                case MatchNotEqual: {
                    EqualityQuery query = new EqualityQuery(matcher.name, matcher.value, false);
                    queries.add(query);
                    break;
                }

                case MatchRegexp: {
                    StringQuery query = new StringQuery(format("_regex:\"%s:%s\"", matcher.name, matcher.value));
                    queries.add(query);
                    break;
                }

                case MatchNotRegexp: {
                    StringQuery query = new StringQuery(format("_regex:\"(?!%s:%s)\"", matcher.name, matcher.value));
                    queries.add(query);
                    break;
                }
            }
        }

        return new Conjunction(queries);
    }

    @Override
    public Query visit(BinaryExpr visitor) {
        Query leftQuery = visit(visitor.lhs);
        Query rightQuery = visit(visitor.rhs);

        ValueType leftValueType = visitor.lhs.valueType();
        ValueType rightValueType = visitor.rhs.valueType();

        if (leftValueType == ValueType.ValueTypeScalar
                && rightValueType == ValueType.ValueTypeScalar) {
            return scalarBinop(visitor.lhs, visitor.rhs, visitor.op);
        } else if (leftValueType == ValueType.ValueTypeVector
                && rightValueType == ValueType.ValueTypeVector) {
            switch (visitor.op) {
                case itemADD: {

                }
            }
        }

        return null;
    }

    @Override
    public Query visit(ParenExpr visitor) {
        return null;
    }

    @Override
    public Query visit(UnaryExpr visitor) {
        Query query = visit(visitor.expr);
        // ValueType valueType = visitor.valueType();

        if (visitor.op == ItemType.itemSUB) {
            if (query instanceof StringQuery) {
                return new StringQuery("-" + ((StringQuery) query).queryStr);
            }
        }

        return query;
    }

    @Override
    public Query visit(NumberLiteral visitor) {
        return new StringQuery(visitor.numberValue());
    }

    @Override
    public Query visit(StringLiteral visitor) {
        // TODO: String - a simple string value; currently unused
        return new StringQuery(String.valueOf(visitor.string));
    }

    @Override
    public Query visit(Call visitor) {
        List<Query> subQueries = Lists.newArrayList();
        for (Expr arg : visitor.args) {
            subQueries.add(visit(arg));
        }

        Function.CallFunction function = visitor.function.call;
        function.call(visitor.args, factory);

        return new Conjunction(subQueries);
    }

    private Query scalarBinop(Expr leftExpr, Expr rightExpr, ItemType itemType) {
        if (leftExpr.exprType == ExprType.NumberLiteral
                && rightExpr.exprType == ExprType.NumberLiteral) {

            NumberLiteral left = (NumberLiteral) leftExpr;
            NumberLiteral right = (NumberLiteral) rightExpr;

            String result;

            switch (itemType) {
                case itemADD:
                    result = String.valueOf(left.number + right.number);
                    break;
                case itemSUB:
                    result = String.valueOf(left.number - right.number);
                    break;
                case itemMUL:
                    result = String.valueOf(left.number * right.number);
                    break;
                case itemDIV:
                    result = String.valueOf(left.number / right.number);
                    break;
                case itemPOW:
                    result = String.valueOf(Math.pow(left.number, right.number));
                    break;
                case itemMOD:
                    result = String.valueOf(left.number % right.number);
                    break;
                case itemEQL:
                    result = String.valueOf(left.number == right.number ? 1 : 0);
                    break;
                case itemNEQ:
                    result = String.valueOf(left.number != right.number ? 1 : 0);
                    break;
                case itemGTR:
                    result = String.valueOf(left.number > right.number ? 1 : 0);
                    break;
                case itemLSS:
                    result = String.valueOf(left.number < right.number ? 1 : 0);
                    break;
                case itemGTE:
                    result = String.valueOf(left.number >= right.number ? 1 : 0);
                    break;
                case itemLTE:
                    result = String.valueOf(left.number <= right.number ? 1 : 0);
                    break;

                default: {
                    throw new RuntimeException(format("operator %s not allowed for Scalar operations", itemType));
                }
            }

            return new StringQuery(trimZeroOfNumber(Double.valueOf(result)));
        }

        throw new RuntimeException("Unsupported Binary Expr " + leftExpr.toString() + " " + itemType.desc() + " " + rightExpr.toString());
    }
}

