package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * intLiteral ::= (-)?[0-9]+
 * </code>
 */
public class IntLiteralExpr extends Expr {
    private final int value;

    public IntLiteralExpr(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                          int value) {
        super(startLocation, endLocation);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitIntLiteralExpr(this);
    }
}
