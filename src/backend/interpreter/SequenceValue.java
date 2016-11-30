package backend.interpreter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A sequence consisting of multiple values, returned by an expression.
 */
public class SequenceValue extends Value {

    // FIXME: Could the values be stored in a plain array since we know the size?
    @NotNull private final List<Value> values;

    SequenceValue(@NotNull List<Value> values) {
        this.values = values;
    }

    @NotNull
    public List<Value> getValues() {
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
