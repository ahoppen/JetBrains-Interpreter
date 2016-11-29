package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.SourceLoc;

public abstract class Expr extends ASTNode {

    @Nullable private Type type;

    public Expr(@NotNull SourceLoc location) {
        super(location);
    }

    @NotNull
    public final Type getType() {
        assert type != null : "Expression has not been type-checked yet";
        return type;
    }

    public void setType(@NotNull Type type) {
        this.type = type;
    }
}
