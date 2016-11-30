package backend.utils;

import backend.AST.*;

public interface ASTVisitor<T> {

    T visitAssignStmt(AssignStmt assignStmt);
    T visitBinaryOperatorExpr(BinaryOperatorExpr binOpExpr);
    T visitFloatLiteralExpr(FloatLiteralExpr floatLiteralExpr);
    T visitIdentifierRefExpr(VariableRefExpr variableRefExpr);
    T visitIntLiteralExpr(IntLiteralExpr intLiteralExpr);
    T visitMapExpr(MapExpr mapExpr);
    T visitOutStmt(OutStmt outExpr);
    T visitParenExpr(ParenExpr parenExpr);
    T visitPrintStmt(PrintStmt printStmt);
    T visitRangeExpr(RangeExpr rangeExpr);
    T visitReduceExpr(ReduceExpr reduceExpr);
}
