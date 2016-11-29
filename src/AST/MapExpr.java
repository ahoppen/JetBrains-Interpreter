package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

public class MapExpr extends Expr {
    @NotNull private final Expr argument;
    @NotNull private final Variable lambdaParam;
    @NotNull private final Expr lambda;

    public MapExpr(@NotNull SourceLoc location, @NotNull Expr argument,
                   @NotNull Variable lambdaParam, @NotNull Expr lambda) {
        super(location);
        this.argument = argument;
        this.lambdaParam = lambdaParam;
        this.lambda = lambda;
    }

    @NotNull
    public Expr getArgument() {
        return argument;
    }

    @NotNull
    public Variable getLambdaParam() {
        return lambdaParam;
    }

    @NotNull
    public Expr getLambda() {
        return lambda;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitMapExpr(this);
    }
}
