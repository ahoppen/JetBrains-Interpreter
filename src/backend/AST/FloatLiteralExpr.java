package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * floatLiteral ::= (-)? ( ([0-9]+.[0-9]*) | ([0-9]*.[0-9]+) )
 * </code>
 */
public final class FloatLiteralExpr extends Expr {
    private final double value;

    public FloatLiteralExpr(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                            double value) {
        super(startLocation, endLocation);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitFloatLiteralExpr(this);
    }
}
