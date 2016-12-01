package backend.interpreter;

import org.jetbrains.annotations.NotNull;

/**
 * A sequence consisting of multiple values, returned by an expression.
 */
public class SequenceValue extends Value {

    @NotNull private final Value[] values;

    SequenceValue(@NotNull Value[] values) {
        this.values = values;
    }

    @NotNull
    public Value[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean firstIteration = true;
        for (Value value : values) {
            if (!firstIteration) {
                sb.append(", ");
            } else {
                firstIteration = false;
            }
            sb.append(value.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
