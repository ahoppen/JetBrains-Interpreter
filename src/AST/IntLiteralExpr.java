package AST;

import AST.Type.IntType;
import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;

public class IntLiteralExpr extends Expr {
    private final int value;

    public IntLiteralExpr(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @NotNull
    @Override
    public Type getType() {
        return IntType.get();
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitIntLiteralExpr(this);
    }
}
