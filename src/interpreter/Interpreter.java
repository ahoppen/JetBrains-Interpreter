package interpreter;

import AST.*;
import org.jetbrains.annotations.NotNull;
import utils.ASTConsumer;
import utils.ASTVisitor;
import utils.Diag;
import utils.Diagnostics;

import java.util.*;

public class Interpreter implements ASTConsumer, ASTVisitor<Value> {

    @NotNull private final Map<Variable, Value> variableValues = new HashMap<>();
    @NotNull private final Map<Stmt, Value> output = new LinkedHashMap<>();

    @Override
    public void consumeStmt(@NotNull Stmt stmt) {
        Value stmtOutput = stmt.acceptVisitor(this);
        if (stmtOutput != null) {
            output.put(stmt, stmtOutput);
        }
    }

    @NotNull
    public Map<Stmt, Value> getOutput() {
        return output;
    }

    private Value evaluateExpr(@NotNull Expr expr) {
        return expr.acceptVisitor(this);
    }

    @Override
    public Value visitAssignStmt(AssignStmt assignStmt) {
        Value value = evaluateExpr(assignStmt.getRhs());
        variableValues.put(assignStmt.getLhs(), value);
        return null;
    }

    @Override
    public Value visitBinaryOperatorExpr(BinaryOperatorExpr binOpExpr) {
        Value lhs = evaluateExpr(binOpExpr.getLhs());
        Value rhs = evaluateExpr(binOpExpr.getRhs());

        if (lhs instanceof ErrorValue || rhs instanceof ErrorValue) {
            return ErrorValue.get();
        } else if (lhs instanceof IntValue && rhs instanceof IntValue) {
            int value;
            switch (binOpExpr.getOp()) {
                case ADD:
                    value = ((IntValue)lhs).getValue() + ((IntValue)rhs).getValue();
                    break;
                case SUB:
                    value = ((IntValue)lhs).getValue() - ((IntValue)rhs).getValue();
                    break;
                case MULT:
                    value = ((IntValue)lhs).getValue() * ((IntValue)rhs).getValue();
                    break;
                case DIV:
                    value = ((IntValue)lhs).getValue() / ((IntValue)rhs).getValue();
                    break;
                case POW:
                    value = (int)Math.pow(((IntValue)lhs).getValue(), ((IntValue)rhs).getValue());
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + binOpExpr.getOp());
            }
            return new IntValue(value);
        } else if (lhs instanceof IntValue && rhs instanceof FloatValue) {
            double value;
            switch (binOpExpr.getOp()) {
                case ADD:
                    value = ((IntValue)lhs).getValue() + ((FloatValue)rhs).getValue();
                    break;
                case SUB:
                    value = ((IntValue)lhs).getValue() - ((FloatValue)rhs).getValue();
                    break;
                case MULT:
                    value = ((IntValue)lhs).getValue() * ((FloatValue)rhs).getValue();
                    break;
                case DIV:
                    value = ((IntValue)lhs).getValue() / ((FloatValue)rhs).getValue();
                    break;
                case POW:
                    value = Math.pow(((IntValue)lhs).getValue(), ((FloatValue)rhs).getValue());
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + binOpExpr.getOp());
            }
            return new FloatValue(value);
        } else if (lhs instanceof FloatValue && rhs instanceof IntValue) {
            double value;
            switch (binOpExpr.getOp()) {
                case ADD:
                    value = ((FloatValue)lhs).getValue() + ((IntValue)rhs).getValue();
                    break;
                case SUB:
                    value = ((FloatValue)lhs).getValue() - ((IntValue)rhs).getValue();
                    break;
                case MULT:
                    value = ((FloatValue)lhs).getValue() * ((IntValue)rhs).getValue();
                    break;
                case DIV:
                    value = ((FloatValue)lhs).getValue() / ((IntValue)rhs).getValue();
                    break;
                case POW:
                    value = Math.pow(((FloatValue)lhs).getValue(), ((IntValue)rhs).getValue());
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + binOpExpr.getOp());
            }
            return new FloatValue(value);
        } else if (lhs instanceof FloatValue && rhs instanceof FloatValue) {
            double value;
            switch (binOpExpr.getOp()) {
                case ADD:
                    value = ((FloatValue)lhs).getValue() + ((FloatValue)rhs).getValue();
                    break;
                case SUB:
                    value = ((FloatValue)lhs).getValue() - ((FloatValue)rhs).getValue();
                    break;
                case MULT:
                    value = ((FloatValue)lhs).getValue() * ((FloatValue)rhs).getValue();
                    break;
                case DIV:
                    value = ((FloatValue)lhs).getValue() / ((FloatValue)rhs).getValue();
                    break;
                case POW:
                    value = Math.pow(((FloatValue)lhs).getValue(), ((FloatValue)rhs).getValue());
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + binOpExpr.getOp());
            }
            return new FloatValue(value);
        } else {
            throw new RuntimeException("Unknown arguments for binary operator: " + lhs + ", " + rhs);
        }
    }

    @Override
    public Value visitFloatLiteralExpr(FloatLiteralExpr floatLiteralExpr) {
        return new FloatValue(floatLiteralExpr.getValue());
    }

    @Override
    public Value visitIdentifierRefExpr(IdentifierRefExpr identifierRefExpr) {
        Value value = variableValues.get(identifierRefExpr.getReferencedVariable());
        if (value == null) {
            return ErrorValue.get();
        } else {
            return value;
        }
    }

    @Override
    public Value visitIntLiteralExpr(IntLiteralExpr intLiteralExpr) {
        return new IntValue(intLiteralExpr.getValue());
    }

    @Override
    public Value visitMapExpr(MapExpr mapExpr) {
        // TODO: Implement this with multiple threads
        Value argument = evaluateExpr(mapExpr.getArgument());
        if (argument instanceof ErrorValue) {
            return ErrorValue.get();
        }
        // The type checker guarantees this is a sequence
        SequenceValue toTransform = (SequenceValue)argument;
        List<Value> transformedValues = new ArrayList<>(toTransform.getValues().size());

        for (Value value : toTransform.getValues()) {
            variableValues.put(mapExpr.getLambdaParam(), value);
            Value transformedValue = evaluateExpr(mapExpr.getLambda());
            if (transformedValue instanceof ErrorValue) {
                return ErrorValue.get();
            }
            transformedValues.add(transformedValue);
        }
        variableValues.remove(mapExpr.getLambdaParam());

        return new SequenceValue(transformedValues);
    }

    @Override
    public Value visitOutStmt(OutStmt outExpr) {
        return evaluateExpr(outExpr.getArgument());
    }

    @Override
    public Value visitParenExpr(ParenExpr parenExpr) {
        return evaluateExpr(parenExpr.getSubExpr());
    }

    @Override
    public Value visitPrintStmt(PrintStmt printStmt) {
        return new StringValue(printStmt.getArgument());
    }

    @Override
    public Value visitRangeExpr(RangeExpr rangeExpr) {
        Value lowerBoundValue = evaluateExpr(rangeExpr.getLowerBound());
        Value upperBoundValue = evaluateExpr(rangeExpr.getUpperBound());
        if (lowerBoundValue instanceof ErrorValue || upperBoundValue instanceof ErrorValue) {
            return ErrorValue.get();
        }
        // The type checker guarantees now that the lower and upper bound are IntValues
        int lowerBound = ((IntValue)lowerBoundValue).getValue();
        int upperBound = ((IntValue)upperBoundValue).getValue();

        if (upperBound <= lowerBound) {
            Diagnostics.error(rangeExpr, Diag.range_upper_bound_smaller_than_lower_bound);
            return ErrorValue.get();
        }

        ArrayList<Value> values = new ArrayList<>(upperBound - lowerBound);
        for (int i = lowerBound; i <= upperBound; i++) {
            values.add(new IntValue(i));
        }
        return new SequenceValue(values);
    }

    @Override
    public Value visitReduceExpr(ReduceExpr reduceExpr) {
        // TODO: Implement this with multiple threads
        Value currentValue = evaluateExpr(reduceExpr.getBase());
        if (currentValue instanceof ErrorValue) {
            return ErrorValue.get();
        }

        Value argument = evaluateExpr(reduceExpr.getSequence());
        if (argument instanceof ErrorValue) {
            return ErrorValue.get();
        }
        // The type checker guarantees this is a sequence
        SequenceValue toTransform = (SequenceValue)argument;

        for (Value value : toTransform.getValues()) {
            variableValues.put(reduceExpr.getLambdaParam1(), currentValue);
            variableValues.put(reduceExpr.getLambdaParam2(), value);
            currentValue = evaluateExpr(reduceExpr.getLambda());

            if (currentValue instanceof ErrorValue) {
                return ErrorValue.get();
            }
        }

        return currentValue;
    }
}
