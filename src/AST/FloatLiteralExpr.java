package AST;

import AST.Type.FloatType;
import AST.Type.Type;
import org.jetbrains.annotations.NotNull;

public class FloatLiteralExpr extends Expr {
    private final double value;

    public FloatLiteralExpr(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @NotNull
    @Override
    public Type getType() {
        return FloatType.get();
    }
}
