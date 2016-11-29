package AST;

import org.jetbrains.annotations.NotNull;
import utils.ASTVisitor;
import utils.SourceLoc;

public abstract class ASTNode {

    @NotNull private final SourceLoc location;

    public ASTNode(@NotNull SourceLoc location) {
        this.location = location;
    }

    @NotNull
    public SourceLoc getLocation() {
        return location;
    }

    public abstract <T> T acceptVisitor(ASTVisitor<T> visitor);

}
