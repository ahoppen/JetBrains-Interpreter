package AST;

import AST.Type.Type;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class RangeExpr extends Expr {
    private final Expr lowerBound;
    private final Expr upperBound;

    public RangeExpr(Expr lowerBound, Expr upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Expr getLowerBound() {
        return lowerBound;
    }

    public Expr getUpperBound() {
        return upperBound;
    }

    @Nullable
    @Override
    public Type getType() {
        return lowerBound.getType();
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitRangeExpr(this);
    }
}
