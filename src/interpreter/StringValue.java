package interpreter;

import org.jetbrains.annotations.NotNull;

public class StringValue extends Value {

    @NotNull private final String value;

    public StringValue(@NotNull String value) {
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
