package parser.ast.value;

import lexer.token.ItemType;
import parser.ast.Expr;
import parser.ast.ExprBinder;
import parser.ast.ExprType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@ExprBinder(type = ExprType.AggregateExpr)
public class AggregateExpr extends Expr {

    // AggregateExpr represents an aggregation operation on a Vector.
    //    type AggregateExpr struct {
    //        Op       itemType // The used aggregation operation.
    //        Expr     Expr     // The Vector expression over which is aggregated.
    //        Param    Expr     // Parameter used by some aggregators.
    //        Grouping []string // The labels by which to group the Vector.
    //        Without  bool     // Whether to drop the given labels rather than keep them.
    //    }

    public ItemType op;
    public Expr expr;
    public Expr param;
    public List<String> grouping;
    public boolean without;

    private AggregateExpr(ItemType op, Expr expr, Expr param, List<String> grouping, boolean without) {
        this.op = op;
        this.expr = expr;
        this.param = param;
        this.grouping = grouping;
        this.without = without;
    }

    @Override
    public String toString() {
        String exprStr = expr.toString();
        if (param != null) {
            exprStr += "," + param.toString();
        }
        String groupingStr = grouping.stream().collect(joining(","));
        return String.format("AggregateExpr<%s,%s,%s,%s>", op.desc(), exprStr, groupingStr, false);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof AggregateExpr)) {
            return false;
        }

        AggregateExpr other = (AggregateExpr) obj;
        return other.hashCode() == hashCode();
    }

    public static AggregateExpr of(ItemType op, Expr expr, Expr param, List<String> grouping, boolean without) {
        return new AggregateExpr(op, expr, param, grouping, without);
    }

    public static AggregateExpr of(ItemType op, Expr expr, List<String> grouping) {
        return new AggregateExpr(op, expr, null, grouping, false);
    }

    public static AggregateExpr of(ItemType op, Expr expr, Expr param) {
        return new AggregateExpr(op, expr, param, Collections.emptyList(), false);
    }

    public static AggregateExpr of(ItemType op, Expr expr) {
        return new AggregateExpr(op, expr, null, Collections.emptyList(), false);
    }

    @Override
    public ValueType valueType() {
        return ValueType.ValueTypeVector;
    }
}
