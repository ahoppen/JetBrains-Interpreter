package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

public abstract class ASTNode {

    @NotNull private final SourceLoc startLocation;
    @NotNull private final SourceLoc endLocation;

    protected ASTNode(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }


    @NotNull
    public SourceLoc getStartLocation() {
        return startLocation;
    }

    @NotNull
    public SourceLoc getEndLocation() {
        return endLocation;
    }

    public abstract <T> T acceptVisitor(ASTVisitor<T> visitor);

}
