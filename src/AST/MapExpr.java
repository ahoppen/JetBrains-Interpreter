package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

/**
 * <code>
 * mapExpr ::= 'map' '(' argument ',' lambdaParam '->' lambda ')'
 * </code>
 * <p>
 * where <code>argument</code> is an expression returning a sequence, <code>lambdaParam</code>
 * is a variable and <code>lambda</code> is an expression that cannot reference any variables except
 * <code>lambdaParam</code>.
 * </p>
 * <p>
 * Evaluating this expression generates a new sequence of the same length as <code>argument</code>
 * by transforming each value in <code>argument</code> according to <code>lambda</code>
 * </p>
 */
public class MapExpr extends Expr {
    /** The sequence to transform */
    @NotNull private final Expr argument;
    /** The variable that contains the value to transform in each lambda iteration */
    @NotNull private final Variable lambdaParam;
    /** The lambda to transform the sequence's values */
    @NotNull private final Expr lambda;

    /**
     * @param location The location of the map keyword in the source code
     * @param argument The sequence to transform
     * @param lambdaParam The variable that contains the value to transform in each lambda iteration
     * @param lambda The lambda to transform the sequence's values
     */
    public MapExpr(@NotNull SourceLoc location, @NotNull Expr argument,
                   @NotNull Variable lambdaParam, @NotNull Expr lambda) {
        super(location);
        this.argument = argument;
        this.lambdaParam = lambdaParam;
        this.lambda = lambda;
    }

    /**
     * @return The sequence to transform
     */
    @NotNull
    public Expr getArgument() {
        return argument;
    }

    /**
     * @return The variable that contains the value to transform in each lambda iteration
     */
    @NotNull
    public Variable getLambdaParam() {
        return lambdaParam;
    }

    /**
     * @return The lambda to transform the sequence's values
     */
    @NotNull
    public Expr getLambda() {
        return lambda;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitMapExpr(this);
    }
}
