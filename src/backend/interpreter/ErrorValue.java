package backend.interpreter;

import org.jetbrains.annotations.NotNull;

/**
 * The fake value used to indicate that an error occurred while evaluating an instruction
 */
public class ErrorValue extends Value {

    @NotNull private static final ErrorValue INSTANCE = new ErrorValue();

    private ErrorValue() {}

    @NotNull
    public static ErrorValue get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "<error>";
    }
}
