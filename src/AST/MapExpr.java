package AST;

import AST.Type.SequenceType;
import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class MapExpr extends Expr {
    @NotNull private final Expr argument;
    @NotNull private final Identifier lambdaParam;
    @NotNull private final Expr lambda;

    public MapExpr(@NotNull Expr argument, @NotNull Identifier lambdaParam, @NotNull Expr lambda) {
        this.argument = argument;
        this.lambdaParam = lambdaParam;
        this.lambda = lambda;
    }

    @NotNull
    public Expr getArgument() {
        return argument;
    }

    @NotNull
    public Identifier getLambdaParam() {
        return lambdaParam;
    }

    @NotNull
    public Expr getLambda() {
        return lambda;
    }

    @Nullable
    @Override
    public Type getType() {
        if (lambda.getType() == null) {
            return null;
        } else {
            return new SequenceType(lambda.getType());
        }
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitMapExpr(this);
    }
}
