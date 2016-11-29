package AST.Type;

public class IntType extends Type {

    private static IntType INSTANCE = new IntType();

    private IntType() {}

    public static IntType get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Int";
    }
}
