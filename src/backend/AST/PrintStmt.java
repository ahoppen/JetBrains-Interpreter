package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * printStmt ::= 'print' stringLiteral
 * </code>
 * <p>
 * where stringLiteral is a string literal wrapped in <code>"</code> that may contain escaped
 * characters.
 * </p>
 * <p>
 * Outputs the string literal upon evaluation
 * </p>
 */
public final class PrintStmt extends Stmt {
    @NotNull
    private final String argument;

    public PrintStmt(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                     @NotNull String argument) {
        super(startLocation, endLocation);
        this.argument = argument;
    }

    @NotNull
    public String getArgument() {
        return argument;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}
