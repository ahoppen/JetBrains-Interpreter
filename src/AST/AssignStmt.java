package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;

public class AssignStmt extends Stmt {
    @NotNull private final Identifier lhs;
    @NotNull private final Expr rhs;

    public AssignStmt(@NotNull Identifier lhs, @NotNull Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @NotNull
    public Identifier getLhs() {
        return lhs;
    }

    @NotNull
    public Expr getRhs() {
        return rhs;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitAssignStmt(this);
    }
}
