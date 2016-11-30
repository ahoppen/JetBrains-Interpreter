package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

public class ParenExpr extends Expr {
    @NotNull private final Expr subExpr;

    public ParenExpr(@NotNull SourceLoc location, @NotNull Expr subExpr) {
        super(location);
        this.subExpr = subExpr;
    }

    @NotNull
    public Expr getSubExpr() {
        return subExpr;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitParenExpr(this);
    }
}
