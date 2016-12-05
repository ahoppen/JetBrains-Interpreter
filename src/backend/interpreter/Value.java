package backend.interpreter;

/**
 * Abstract base class for values that are returned by an expression and can be assigned to
 * variables
 */
public abstract class Value {

    private boolean recyclable = true;

    /**
     * @return <code>true</code> if this value is not stored anywhere for future retrieval
     *         (e.g. a variable)
     */
    final boolean isRecyclable() {
        return recyclable;
    }

    final void setRecyclable(boolean recyclable) {
        this.recyclable = recyclable;
    }
}
