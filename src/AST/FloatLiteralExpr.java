package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

/**
 * <code>
 * floatLiteral ::= (-)? ( ([0-9]+.[0-9]*) | ([0-9]*.[0-9]+) )
 * </code>
 */
public class FloatLiteralExpr extends Expr {
    private final double value;

    public FloatLiteralExpr(@NotNull SourceLoc location, double value) {
        super(location);
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
