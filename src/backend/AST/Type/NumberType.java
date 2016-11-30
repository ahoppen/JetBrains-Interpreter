package backend.AST.Type;

/**
 * A float or int type
 */
public class NumberType extends Type {

    private static final NumberType INSTANCE = new NumberType();

    private NumberType() {}

    public static NumberType get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Number";
    }
}
