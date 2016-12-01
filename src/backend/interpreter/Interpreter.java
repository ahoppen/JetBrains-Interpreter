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

    @NotNull private final Diagnostics diagnostics;

    /** The current values of all variables valid in the current scope */
    @NotNull private final Map<Variable, Value> variableValues = new HashMap<>();
    /** The output of all statements consumed so far */
    @NotNull private final Map<Stmt, Value> output = new LinkedHashMap<>();

    /**
     * Constantly allocating new objects for values is inefficient as it triggers the garbage
     * collector. Thus add values that are no longer used to these Stacks from which they can be
     * recycled
     */
    private Stack<IntValue> recycledIntValues = new Stack<>();
    private Stack<FloatValue> recycledFloatValues = new Stack<>();

    public Interpreter(@NotNull Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * Create a new {@link IntValue} either from the recycling bag or by allocating a new object
     * @param value The payload of the {@link IntValue}
     * @return An {@link IntValue} with the given value
     */
    private IntValue createIntValue(int value) {
        if (!recycledIntValues.empty()) {
            IntValue recycledValue = recycledIntValues.pop();
            recycledValue.setValue(value);
            return recycledValue;
        } else {
            return new IntValue(value);
        }
    }

    /**
     * Recycle an {@link IntValue} if it has been marked as recyclable. If the value is recyclable
     * referencing it after it has been recycled results in undefined behaviour.
     * @param value The value to recycle
     */
    private void recycle(IntValue value) {
        if (value.isRecyclable()) {
            recycledIntValues.push(value);
        }
    }

    /**
     * Create a new {@link FloatValue} either from the recycling bag or by allocating a new object
     * @param value The payload of the {@link FloatValue}
     * @return An {@link FloatValue} with the given value
     */
    private FloatValue createFloatValue(double value) {
        if (!recycledFloatValues.empty()) {
            FloatValue recycledValue = recycledFloatValues.pop();
            recycledValue.setValue(value);
            return recycledValue;
        } else {
            return new FloatValue(value);
        }
    }

    /**
     * Recycle an {@link FloatValue} if it has been marked as recyclable. If the value is recyclable
     * referencing it after it has been recycled results in undefined behaviour.
     * @param value The value to recycle
     */
    private void recycle(FloatValue value) {
        if (value.isRecyclable()) {
            recycledFloatValues.push(value);
        }
    }

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
        value.setRecyclable(false);
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
                    int lhsValue = ((IntValue)lhs).getValue();
                    int rhsValue = ((IntValue)rhs).getValue();
                    if (lhsValue % rhsValue == 0) {
                        // Division results in an integer -> return an IntValue
                        value = lhsValue / rhsValue;
                        break;
                    } else {
                        recycle((IntValue)lhs);
                        recycle((IntValue)rhs);
                        double doubleValue = (double)lhsValue / rhsValue;
                        return createFloatValue(doubleValue);
                    }
                }
                case POW: {
                    int base = ((IntValue)lhs).getValue();
                    int exponent = ((IntValue)rhs).getValue();
                    if (exponent >= 0) {
                        // Exponentiation results in an integer if exponent >= 0
                        value = (int)Math.pow(base, exponent);
                        break;
                    } else {
                        recycle((IntValue)lhs);
                        recycle((IntValue)rhs);
                        double doubleValue = Math.pow(base, exponent);
                        return createFloatValue(doubleValue);
                    }
                }
                default:
                    throw new RuntimeException("Unknown operator: " + binOpExpr.getOp());
            }
            recycle((IntValue)lhs);
            recycle((IntValue)rhs);
            return createIntValue(value);
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
            recycle((IntValue)lhs);
            recycle((FloatValue)rhs);
            return createFloatValue(value);
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
            recycle((FloatValue)lhs);
            recycle((IntValue)rhs);
            return createFloatValue(value);
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
            recycle((FloatValue)lhs);
            recycle((FloatValue)rhs);
            return createFloatValue(value);
        } else {
            throw new RuntimeException("Unknown arguments for binary operator: " + lhs + ", " + rhs);
        }
    }

    @Override
    public Value visitFloatLiteralExpr(FloatLiteralExpr floatLiteralExpr) {
        return createFloatValue(floatLiteralExpr.getValue());
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
        return createIntValue(intLiteralExpr.getValue());
    }

    @Override
    public Value visitMapExpr(MapExpr mapExpr) {
        Value argument = evaluateExpr(mapExpr.getArgument());
        if (argument instanceof ErrorValue) {
            return ErrorValue.get();
        }
        // The type checker guarantees that the argument is a sequence
        SequenceValue toTransform = (SequenceValue)argument;

        // Accumulate the transformed values in this list
        Value[] transformedValues = new Value[toTransform.getValues().length];
        boolean[] errorOccurred = new boolean[] {false};

        boolean valuesRecyclable = argument.isRecyclable();

        final int numberOfThreads = Runtime.getRuntime().availableProcessors();

        Thread[] threads = new Thread[numberOfThreads];
        for (int j = 0; j < numberOfThreads; j++) {
            final int finalJ = j;
            threads[j] = new Thread(() -> {
                Interpreter subInterpreter = new Interpreter(diagnostics);
                Value[] values = toTransform.getValues();
                int size = values.length;
                for (int i = 0; i < size; i++) {
                    Value value = values[i];
                    if (i % numberOfThreads == finalJ) {
                        // The value is used as a variable in the lambda and can thus not be
                        // recycled while evaluating the lambda
                        value.setRecyclable(false);
                        subInterpreter.variableValues.put(mapExpr.getLambdaParam(), value);
                        // Set the variable's value and evaluate the expression with this value
                        Value transformedValue = subInterpreter.evaluateExpr(mapExpr.getLambda());
                        if (transformedValue instanceof ErrorValue) {
                            errorOccurred[0] = true;
                            break;
                        }
                        transformedValues[i] = transformedValue;

                        // Recycle the value in the subInterpreter since it tends to need the value
                        // for the next lambda execution
                        if (valuesRecyclable) {
                            value.setRecyclable(true);
                            if (value instanceof IntValue) {
                                subInterpreter.recycle((IntValue)value);
                            } else if (value instanceof FloatValue) {
                                subInterpreter.recycle((FloatValue)value);
                            }
                        }
                    }
                }
            });
            threads[j].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

        if (errorOccurred[0]) {
            return ErrorValue.get();
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
            diagnostics.error(rangeExpr.getLowerBound(), Diag.lower_bound_of_range_not_int,
                    "Float");
            return ErrorValue.get();
        }
        if (!(upperBoundValue instanceof IntValue)) {
            diagnostics.error(rangeExpr.getUpperBound(), Diag.upper_bound_of_range_not_int,
                    "Float");
            return ErrorValue.get();
        }
        int lowerBound = ((IntValue)lowerBoundValue).getValue();
        int upperBound = ((IntValue)upperBoundValue).getValue();

        if (upperBound < lowerBound) {
            diagnostics.error(rangeExpr, Diag.range_upper_bound_smaller_than_lower_bound);
            return ErrorValue.get();
        }

        Value[] values = new Value[upperBound - lowerBound + 1];
        int arrayIndex = 0;
        for (int i = lowerBound; i <= upperBound; i++) {
            IntValue v = createIntValue(i);
            values[arrayIndex] = v;
            arrayIndex++;
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
