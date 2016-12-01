package backend.interpreter;

/**
 * An integer value returned by an expression
 */
public class IntValue extends Value {

    private int value;

    IntValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
