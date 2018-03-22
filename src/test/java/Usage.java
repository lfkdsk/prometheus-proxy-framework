import lombok.NonNull;
import parser.Parser;
import parser.ast.Expr;

public final class Usage {
    public static Expr parseExpr(@NonNull String input) {
        return Parser.parseExpr(input);
    }
}
