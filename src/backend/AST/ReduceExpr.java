package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * reduceExpr ::= 'reduce' '(' sequence ',' base ',' lambdaParam1 ' ' lambdaParam2 '->' lambda ')'
 * </code>
 * <p>
 * where <code>sequence</code> is an expression returning a sequence, <code>base</code> is an
 * expression returning the base type of the sequence, <code>lambdaParam1</code> and
 * <code>lambdaParam2</code> are variables and <code>lambda</code> is an expression that will
 * combine <code>lambdaParam1</code> and <code>lambdaParam2</code> into one value of the same
 * type as <code>base</code>
 * </p>
 * <p>
 * Will compute the functional reduce instruction, combining the sequence into a single value, e.g.
 * <code>reduce({1, 5}, 1, x y -> x * y</code> will compute <code>1 * 1 * 2 * 3 * 4 * 5</code>
 * </p>
 * <p>
 * For efficiency reasons in evaluation the lambda is assumed to be associative
 * </p>
 */
public class ReduceExpr extends Expr {
    @NotNull private final Expr base;
    @NotNull private final Expr sequence;
    @NotNull private final Variable lambdaParam1;
    @NotNull private final Variable lambdaParam2;
    @NotNull private final Expr lambda;

    public ReduceExpr(@NotNull SourceLoc location, @NotNull Expr base, @NotNull Expr sequence,
                      @NotNull Variable lambdaParam1, @NotNull Variable lambdaParam2,
                      @NotNull Expr lambda) {
        super(location);
        this.base = base;
        this.sequence = sequence;
        this.lambdaParam1 = lambdaParam1;
        this.lambdaParam2 = lambdaParam2;
        this.lambda = lambda;
    }

    @NotNull
    public Expr getBase() {
        return base;
    }

    @NotNull
    public Expr getSequence() {
        return sequence;
    }

    @NotNull
    public Variable getLambdaParam1() {
        return lambdaParam1;
    }

    @NotNull
    public Variable getLambdaParam2() {
        return lambdaParam2;
    }

    @NotNull
    public Expr getLambda() {
        return lambda;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitReduceExpr(this);
    }
}
