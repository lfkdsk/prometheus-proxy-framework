package parser.ast;

public enum ExprType {
    NumberLiteral,
    StringLiteral,
    BinaryExpr,
    ParenExpr,
    UnaryExpr,
    VectorSelector,
    MatrixSelector;
}
