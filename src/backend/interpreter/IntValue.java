package backend.interpreter;

/**
 * An integer value returned by an expression
 */
public class IntValue extends Value {

    private final int value;

    IntValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
