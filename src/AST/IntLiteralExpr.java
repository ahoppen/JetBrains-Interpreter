package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

/**
 * <code>
 * intLiteral ::= (-)?[0-9]+
 * </code>
 */
public class IntLiteralExpr extends Expr {
    private final int value;

    public IntLiteralExpr(@NotNull SourceLoc location, int value) {
        super(location);
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
