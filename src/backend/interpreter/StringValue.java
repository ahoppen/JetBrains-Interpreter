package backend.interpreter;

import org.jetbrains.annotations.NotNull;

public class StringValue extends Value {

    @NotNull private final String value;

    StringValue(@NotNull String value) {
        this.value = value;
    }

    @NotNull
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
