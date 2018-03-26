package io.dashbase.parser.ast;

public enum ExprType {
    NumberLiteral,
    StringLiteral,
    BinaryExpr,
    ParenExpr,
    UnaryExpr,
    Call,
    VectorSelector,
    MatrixSelector,
    AggregateExpr;
}
