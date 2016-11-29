package AST.Type;

public class FloatType extends Type {

    private static FloatType INSTANCE = new FloatType();

    private FloatType() {}

    public static FloatType get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Float";
    }
}
