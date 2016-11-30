package backend.interpreter;

import backend.AST.*;
import org.jetbrains.annotations.NotNull;
import backend.utils.ASTConsumer;
import backend.utils.ASTVisitor;
import backend.errorHandling.Diag;
import backend.errorHandling.Diagnostics;

import java.util.*;

/**
 * Interprets the statements it consumes, saving the output of each statement in a map that can be
 * retrieved using {@link #getOutput()}
 */
public class Interpreter implements ASTConsumer, ASTVisitor<Value> {

    /** The current values of all variables valid in the current scope */
    @NotNull private final Map<Variable, Value> variableValues = new HashMap<>();
    /** The output of all statements consumed so far */
    @NotNull private final Map<Stmt, Value> output = new LinkedHashMap<>();

    @Override
    public void consumeStmt(@NotNull Stmt stmt) {
        Value stmtOutput = stmt.acceptVisitor(this);
        // If the statement produces output (i.e. 'print' and 'out') save it to the ouputs
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
            // If both operands are integers, the result is often also an integer
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
                case DIV: {
                    // Division might result in a fraction, hence return a double value
                    // FIXME: Return an integer if division is possible without remainder
                    double doubleValue = (double)((IntValue)lhs).getValue() /
                            ((IntValue)rhs).getValue();
                    return new FloatValue(doubleValue);
                }
                case POW: {
                    int base = ((IntValue)lhs).getValue();
                    int exponent = ((IntValue)rhs).getValue();
                    if (exponent >= 0) {
                        // Exponentiation results in an integer if exponent >= 0
                        value = (int)Math.pow(base, exponent);
                        break;
                    } else {
                        double doubleValue = Math.pow(base, exponent);
                        return new FloatValue(doubleValue);
                    }
                }
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
    public Value visitIdentifierRefExpr(VariableRefExpr variableRefExpr) {
        Value value = variableValues.get(variableRefExpr.getReferencedVariable());
        if (value == null) {
            throw new RuntimeException("Variable " + variableRefExpr.getReferencedVariable() +
                    "has no value although the type checker should have enforced it");
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
        // The type checker guarantees that the argument is a sequence
        SequenceValue toTransform = (SequenceValue)argument;

        // Accumulate the transformed values in this list
        List<Value> transformedValues = new ArrayList<>(toTransform.getValues().size());

        for (Value value : toTransform.getValues()) {
            // Set the variable's value and evaluate the expression with this value
            variableValues.put(mapExpr.getLambdaParam(), value);
            Value transformedValue = evaluateExpr(mapExpr.getLambda());
            if (transformedValue instanceof ErrorValue) {
                return ErrorValue.get();
            }
            transformedValues.add(transformedValue);
        }

        // The lambda param is no longer valid after the lambda's scope -> remove it
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
        if (!(lowerBoundValue instanceof IntValue)) {
            Diagnostics.error(rangeExpr.getLowerBound(), Diag.lower_bound_of_range_not_int,
                    "Float");
            return ErrorValue.get();
        }
        if (!(upperBoundValue instanceof IntValue)) {
            Diagnostics.error(rangeExpr.getUpperBound(), Diag.upper_bound_of_range_not_int,
                    "Float");
            return ErrorValue.get();
        }
        int lowerBound = ((IntValue)lowerBoundValue).getValue();
        int upperBound = ((IntValue)upperBoundValue).getValue();

        if (upperBound < lowerBound) {
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
            // Inject the lambda parameter's values and evaluate the value to calculate the new
            // current value
            variableValues.put(reduceExpr.getLambdaParam1(), currentValue);
            variableValues.put(reduceExpr.getLambdaParam2(), value);
            currentValue = evaluateExpr(reduceExpr.getLambda());

            if (currentValue instanceof ErrorValue) {
                return ErrorValue.get();
            }
        }

        // The lambda parameter's values are no longer valid after the lambda's scope -> remove them
        variableValues.remove(reduceExpr.getLambdaParam1());
        variableValues.remove(reduceExpr.getLambdaParam2());

        return currentValue;
    }
}
