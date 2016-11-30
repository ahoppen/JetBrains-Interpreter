package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

/**
 * <code>
 * outStmt ::= 'out' argument
 * </code>
 * <p>where <code>argument</code> is an expression</p>
 * <p>Prints the evaluated value of <code>argument</code></p>
 */
public class OutStmt extends Stmt {
    @NotNull
    private final Expr argument;

    public OutStmt(@NotNull SourceLoc location, @NotNull Expr argument) {
        super(location);
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
