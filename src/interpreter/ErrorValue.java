package interpreter;

import org.jetbrains.annotations.NotNull;

public class ErrorValue extends Value {

    @NotNull private static final ErrorValue INSTANCE = new ErrorValue();

    private ErrorValue() {
    }

    @NotNull
    public static ErrorValue get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "<error>";
    }
}
