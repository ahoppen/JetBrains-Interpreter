package interpreter;

/**
 * A floating point value returned by an expression
 */
public class FloatValue extends Value {

    private final double value;

    FloatValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
