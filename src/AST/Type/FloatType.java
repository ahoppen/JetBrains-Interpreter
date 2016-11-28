package AST.Type;

public class FloatType extends Type {

    private static FloatType INSTANCE;

    private FloatType() {}

    public static FloatType get() {
        return INSTANCE;
    }
}
