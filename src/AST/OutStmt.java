package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;

public class OutStmt extends Stmt {
    @NotNull
    private final Expr argument;

    public OutStmt(@NotNull Expr argument) {
        this.argument = argument;
    }

    @NotNull
    public Expr getArgument() {
        return argument;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitOutStmt(this);
    }
}
