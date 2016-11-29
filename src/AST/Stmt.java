package AST;

import org.jetbrains.annotations.NotNull;
import utils.SourceLoc;

public abstract class Stmt extends ASTNode {
    public Stmt(@NotNull SourceLoc location) {
        super(location);
    }
}
