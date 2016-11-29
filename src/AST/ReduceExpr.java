package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

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
