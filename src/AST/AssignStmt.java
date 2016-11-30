package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

/**
 * <code>
 * assign ::= var lhs = rhs
 * </code>
 *
 * <p>where <code>lhs</code> is an identifier and <code>rhs</code> is an expression</p>
 */
public class AssignStmt extends Stmt {
    @NotNull private final Variable lhs;
    @NotNull private final Expr rhs;

    public AssignStmt(@NotNull SourceLoc location, @NotNull Variable lhs, @NotNull Expr rhs) {
        super(location);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @NotNull
    public Variable getLhs() {
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
