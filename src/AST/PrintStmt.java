package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

public class PrintStmt extends Stmt {
    @NotNull
    private final String argument;

    public PrintStmt(@NotNull SourceLoc location, @NotNull String argument) {
        super(location);
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
