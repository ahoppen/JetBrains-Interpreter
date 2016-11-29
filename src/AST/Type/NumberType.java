package AST.Type;

public class NumberType extends Type {

    private static NumberType INSTANCE = new NumberType();

    private NumberType() {}

    public static NumberType get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Number";
    }
}
