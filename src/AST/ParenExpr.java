package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class ParenExpr extends Expr {
    @NotNull private final Expr subExpr;

    public ParenExpr(@NotNull Expr subExpr) {
        this.subExpr = subExpr;
    }

    @NotNull
    public Expr getSubExpr() {
        return subExpr;
    }

    @Nullable
    @Override
    public Type getType() {
        return subExpr.getType();
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitParenExpr(this);
    }
}
