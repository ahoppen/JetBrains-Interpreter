package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;

public class PrintStmt extends Stmt {
    @NotNull
    private final String argument;

    public PrintStmt(@NotNull String argument) {
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
