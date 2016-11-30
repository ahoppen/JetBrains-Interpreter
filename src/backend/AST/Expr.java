package backend.AST;

import backend.AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import backend.utils.SourceLoc;

public abstract class Expr extends ASTNode {

    @Nullable private Type type;

    public Expr(@NotNull SourceLoc location) {
        super(location);
    }

    /**
     * Returns the type of the result of this expression <b>after</b> it has been type checked.
     * Calling this method before type checking (i.e. before {@link #setType(Type)} has been called)
     * results in an assertion error
     * @return The type this expression returns
     */
    @NotNull
    public final Type getType() {
        assert type != null : "Expression has not been type-checked yet";
        return type;
    }

    /**
     * Set the type this expression returns, thereby marking it as type-checked
     * @param type The type this expression returns
     */
    public void setType(@NotNull Type type) {
        this.type = type;
    }
}
