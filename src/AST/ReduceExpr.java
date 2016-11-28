package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class ReduceExpr extends Expr {
    @NotNull private final Expr base;
    @NotNull private final Expr sequence;
    @NotNull private final Identifier lambdaParam1;
    @NotNull private final Identifier lambdaParam2;
    @NotNull private final Expr lambda;

    public ReduceExpr(@NotNull Expr base, @NotNull Expr sequence, @NotNull Identifier lambdaParam1,
                      @NotNull Identifier lambdaParam2, @NotNull Expr lambda) {
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
    public Identifier getLambdaParam1() {
        return lambdaParam1;
    }

    @NotNull
    public Identifier getLambdaParam2() {
        return lambdaParam2;
    }

    @NotNull
    public Expr getLambda() {
        return lambda;
    }

    @Nullable
    @Override
    public Type getType() {
        return base.getType();
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitReduceExpr(this);
    }
}
