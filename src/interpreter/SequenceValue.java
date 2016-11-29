package interpreter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SequenceValue extends Value {

    @NotNull private final List<Value> values;

    public SequenceValue(@NotNull List<Value> values) {
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
