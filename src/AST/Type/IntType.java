package AST.Type;

public class IntType extends Type {

    private static IntType INSTANCE;

    private IntType() {}

    public static IntType get() {
        return INSTANCE;
    }
}
