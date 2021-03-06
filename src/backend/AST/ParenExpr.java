package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * parenExpr ::= '(' subExpr ')'
 * </code>
 * <p>Represents an expression that was wrapped in parenthesis in the source code</p>
 */
public final class ParenExpr extends Expr {
    @NotNull private final Expr subExpr;

    public ParenExpr(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                     @NotNull Expr subExpr) {
        super(startLocation, endLocation);
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
