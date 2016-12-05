package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * outStmt ::= 'out' argument
 * </code>
 * <p>where <code>argument</code> is an expression</p>
 * <p>Prints the evaluated value of <code>argument</code></p>
 */
public final class OutStmt extends Stmt {
    @NotNull
    private final Expr argument;

    public OutStmt(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                   @NotNull Expr argument) {
        super(startLocation, endLocation);
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
