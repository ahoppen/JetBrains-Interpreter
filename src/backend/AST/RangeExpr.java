package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * rangeExpr ::= '{' lowerBound ',' upperBound '}'
 * </code>
 * <p>
 * where <code>lowerBound</code> and <code>upperBound</code> are expressions.
 * </p>
 * <p>
 * Creates a closed sequence <code>[lowerBound, upperBound]</code>. Both bounds must be integers. If
 * <code>upperBound < lowerBound</code> this will result in a runtime error
 * </p>
 */
public final class RangeExpr extends Expr {
    @NotNull private final Expr lowerBound;
    @NotNull private final Expr upperBound;

    public RangeExpr(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                     @NotNull Expr lowerBound, @NotNull Expr upperBound) {
        super(startLocation, endLocation);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @NotNull
    public Expr getLowerBound() {
        return lowerBound;
    }

    @NotNull
    public Expr getUpperBound() {
        return upperBound;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitRangeExpr(this);
    }
}
