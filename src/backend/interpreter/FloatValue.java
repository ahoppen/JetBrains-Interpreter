package backend.interpreter;

/**
 * A floating point value returned by an expression
 */
public final class FloatValue extends Value {

    private double value;

    FloatValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
