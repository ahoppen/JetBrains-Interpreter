package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;
import utils.SourceLoc;

public class RangeExpr extends Expr {
    @NotNull private final Expr lowerBound;
    @NotNull private final Expr upperBound;

    public RangeExpr(@NotNull SourceLoc location, @NotNull Expr lowerBound,
                     @NotNull Expr upperBound) {
        super(location);
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
