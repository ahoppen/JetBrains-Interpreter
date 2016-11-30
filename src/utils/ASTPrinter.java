package utils;

import AST.*;
import org.jetbrains.annotations.NotNull;

public class ASTPrinter implements ASTConsumer, ASTVisitor<Void> {

    private int indentation = 0;

    @Override
    public void consumeStmt(@NotNull Stmt stmt) {
        visit(stmt);
    }

    private void visit(ASTNode node) {
        node.acceptVisitor(this);
    }

    private void increaseIndentation() {
        indentation += 1;
    }

    private void decreaseIndentation() {
        assert indentation > 0;
        indentation -= 1;
    }

    private void print(String str) {
        for (int i = 0; i < indentation; i++) {
            System.out.print("  ");
        }
        System.out.println(str);
    }

    @Override
    public Void visitAssignStmt(@NotNull AssignStmt assignStmt) {
        print("(assignExpr var=" + assignStmt.getLhs().getName());
        increaseIndentation();
        visit(assignStmt.getRhs());
        decreaseIndentation();
        print(")");
        return null;
    }

    @Override
    public Void visitBinaryOperatorExpr(BinaryOperatorExpr binOpExpr) {
        print("(binaryOperatorExpr op=" + binOpExpr.getOp());
        increaseIndentation();
        visit(binOpExpr.getLhs());
        visit(binOpExpr.getRhs());
        decreaseIndentation();
        print(")");
        return null;
    }

    @Override
    public Void visitFloatLiteralExpr(FloatLiteralExpr floatLiteralExpr) {
        print("(floatLiteralExpr value=" + floatLiteralExpr.getValue() + ")");
        return null;
    }

    @Override
    public Void visitIdentifierRefExpr(VariableRefExpr variableRefExpr) {
        print("(variableRefExpr identifier=" + variableRefExpr.getVariableName() + ")");
        return null;
    }

    @Override
    public Void visitIntLiteralExpr(IntLiteralExpr intLiteralExpr) {
        print("(intLiteralExpr value=" + intLiteralExpr.getValue() + ")");
        return null;
    }

    @Override
    public Void visitMapExpr(MapExpr mapExpr) {
        print("(mapExpr param=" + mapExpr.getLambdaParam().getName());
        increaseIndentation();
        visit(mapExpr.getArgument());
        visit(mapExpr.getLambda());
        decreaseIndentation();
        print(")");
        return null;
    }

    @Override
    public Void visitOutStmt(OutStmt outExpr) {
        print("(outExpr");
        increaseIndentation();
        visit(outExpr.getArgument());
        decreaseIndentation();
        print(")");
        return null;
    }

    @Override
    public Void visitParenExpr(ParenExpr parenExpr) {
        print("(parenExpr");
        increaseIndentation();
        visit(parenExpr.getSubExpr());
        decreaseIndentation();
        print(")");
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        print("(printExpr string=" + printStmt.getArgument() + ")");
        return null;
    }

    @Override
    public Void visitRangeExpr(RangeExpr rangeExpr) {
        print("(rangeExpr");
        increaseIndentation();
        visit(rangeExpr.getLowerBound());
        visit(rangeExpr.getUpperBound());
        decreaseIndentation();
        print(")");
        return null;
    }

    @Override
    public Void visitReduceExpr(ReduceExpr reduceExpr) {
        print("(reduceExpr param1=" + reduceExpr.getLambdaParam1().getName() +
                " param2=" + reduceExpr.getLambdaParam2().getName());
        increaseIndentation();
        visit(reduceExpr.getBase());
        visit(reduceExpr.getSequence());
        visit(reduceExpr.getLambda());
        decreaseIndentation();
        print(")");
        return null;
    }


}
