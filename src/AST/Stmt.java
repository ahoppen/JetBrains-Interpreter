package AST;

import org.jetbrains.annotations.NotNull;
import utils.SourceLoc;

/**
 * A statement in the program that may have side-effects but does not return a value
 */
public abstract class Stmt extends ASTNode {
    public Stmt(@NotNull SourceLoc location) {
        super(location);
    }
}
