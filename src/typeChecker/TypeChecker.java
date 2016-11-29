package typeChecker;

import AST.*;
import AST.Type.NumberType;
import AST.Type.SequenceType;
import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import utils.ASTConsumer;
import utils.ASTVisitor;
import utils.Diag;
import utils.Diagnostics;

public class TypeChecker implements ASTConsumer, ASTVisitor<Boolean> {

    @NotNull private ASTConsumer nextConsumer;
    @NotNull private VariableScope variableScope = new VariableScope(null);

    public TypeChecker(@NotNull ASTConsumer nextConsumer) {
        this.nextConsumer = nextConsumer;
    }

    private Boolean typeCheck(ASTNode node) {
        return node.acceptVisitor(this);
    }

    @Override
    public void consumeStmt(@NotNull Stmt stmt) {
        typeCheck(stmt);
        nextConsumer.consumeStmt(stmt);
    }

    @Override
    public Boolean visitAssignStmt(AssignStmt assignStmt) {
        if (!typeCheck(assignStmt.getRhs())) {
            return false;
        }
        if (variableScope.isVariableDeclared(assignStmt.getLhs().getName())) {
            Diagnostics.error(assignStmt, Diag.variable_already_declared,
                    assignStmt.getLhs().getName());
            return false;
        }

        variableScope.declareVariable(assignStmt.getLhs());
        assignStmt.getLhs().setType(assignStmt.getRhs().getType());
        return true;
    }

    @Override
    public Boolean visitBinaryOperatorExpr(BinaryOperatorExpr binOpExpr) {
        if (!typeCheck(binOpExpr.getLhs()) || !typeCheck(binOpExpr.getRhs())) {
            return false;
        }
        Type lhsType = binOpExpr.getLhs().getType();
        Type rhsType = binOpExpr.getRhs().getType();
        if (!(lhsType instanceof NumberType) ||
                !(rhsType instanceof NumberType)) {
            Diagnostics.error(binOpExpr, Diag.arithmetic_operator_on_non_number,
                    binOpExpr.getOp().toSourceString(), lhsType, rhsType);
            return false;
        }

        binOpExpr.setType(NumberType.get());
        return true;
    }

    @Override
    public Boolean visitFloatLiteralExpr(FloatLiteralExpr floatLiteralExpr) {
        floatLiteralExpr.setType(NumberType.get());
        return true;
    }

    @Override
    public Boolean visitIdentifierRefExpr(IdentifierRefExpr identifierRefExpr) {
        Variable variable = variableScope.lookupVariable(identifierRefExpr.getIdentifier());
        if (variable == null) {
            Diagnostics.error(identifierRefExpr, Diag.undeclared_variable,
                    identifierRefExpr.getIdentifier());
            return false;
        }

        identifierRefExpr.setType(variable.getType());
        identifierRefExpr.setReferencedVariable(variable);
        return true;
    }

    @Override
    public Boolean visitIntLiteralExpr(IntLiteralExpr intLiteralExpr) {
        intLiteralExpr.setType(NumberType.get());
        return true;
    }

    @Override
    public Boolean visitMapExpr(MapExpr mapExpr) {
        if (!typeCheck(mapExpr.getArgument())) {
            return false;
        }
        Type argumentType = mapExpr.getArgument().getType();
        if (!(argumentType instanceof SequenceType)) {
            Diagnostics.error(mapExpr.getArgument(), Diag.argument_of_map_not_sequence,
                    argumentType);
            return false;
        }
        mapExpr.getLambdaParam().setType(((SequenceType)argumentType).getSubType());

        variableScope = new VariableScope(variableScope);
        variableScope.declareVariable(mapExpr.getLambdaParam());

        boolean lambdaTypeCheckError = !typeCheck(mapExpr.getLambda());

        assert variableScope.getOuterScope() != null;
        variableScope = variableScope.getOuterScope();

        if (lambdaTypeCheckError) {
            return false;
        }

        mapExpr.setType(new SequenceType(mapExpr.getLambda().getType()));
        return true;
    }

    @Override
    public Boolean visitOutStmt(OutStmt outStmt) {
        return typeCheck(outStmt.getArgument());
    }

    @Override
    public Boolean visitParenExpr(ParenExpr parenExpr) {
        if (!typeCheck(parenExpr.getSubExpr())) {
            return false;
        }

        parenExpr.setType(parenExpr.getSubExpr().getType());
        return true;
    }

    @Override
    public Boolean visitPrintStmt(PrintStmt printStmt) {
        return true;
    }

    @Override
    public Boolean visitRangeExpr(RangeExpr rangeExpr) {
        if (!typeCheck(rangeExpr.getLowerBound()) || !typeCheck(rangeExpr.getUpperBound())) {
            return false;
        }
        if (!(rangeExpr.getLowerBound().getType() instanceof NumberType)) {
            Diagnostics.error(rangeExpr.getLowerBound(), Diag.lower_bound_of_range_not_int,
                    rangeExpr.getLowerBound().getType());
            return false;
        }
        if (!(rangeExpr.getUpperBound().getType() instanceof NumberType)) {
            Diagnostics.error(rangeExpr.getUpperBound(), Diag.upper_bound_of_range_not_int,
                    rangeExpr.getUpperBound().getType());
            return false;
        }
        rangeExpr.setType(new SequenceType(NumberType.get()));
        return true;
    }

    @Override
    public Boolean visitReduceExpr(ReduceExpr reduceExpr) {
        if (!typeCheck(reduceExpr.getBase()) | !typeCheck(reduceExpr.getSequence())) {
            return false;
        }
        if (!(reduceExpr.getSequence().getType() instanceof SequenceType)) {
            Diagnostics.error(reduceExpr.getSequence(), Diag.argument_of_reduce_not_sequence,
                    reduceExpr.getSequence().getType());
            return false;
        }
        Type sequenceBaseType = ((SequenceType)reduceExpr.getSequence().getType()).getSubType();
        Type baseType = reduceExpr.getBase().getType();

        reduceExpr.getLambdaParam1().setType(baseType);
        reduceExpr.getLambdaParam2().setType(sequenceBaseType);

        variableScope = new VariableScope(variableScope);
        variableScope.declareVariable(reduceExpr.getLambdaParam1());
        variableScope.declareVariable(reduceExpr.getLambdaParam2());

        boolean lambdaTypeCheckError = !typeCheck(reduceExpr.getLambda());

        if (!reduceExpr.getLambda().getType().equals(baseType)) {
            Diagnostics.error(reduceExpr.getLambda(),
                    Diag.lambda_of_reduce_does_not_return_base_type, baseType,
                    reduceExpr.getLambda().getType());
            lambdaTypeCheckError = true;
        }

        assert variableScope.getOuterScope() != null;
        variableScope = variableScope.getOuterScope();

        if (lambdaTypeCheckError) {
            return false;
        }

        reduceExpr.setType(baseType);
        return true;
    }
}
