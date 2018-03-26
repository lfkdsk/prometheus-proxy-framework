import lombok.NonNull;
import io.dashbase.parser.Parser;
import io.dashbase.parser.ast.Expr;

public final class Usage {
    public static Expr parseExpr(@NonNull String input) {
        return Parser.parseExpr(input);
    }
}
