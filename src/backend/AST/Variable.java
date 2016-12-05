package backend.AST;

import backend.AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A particular variable that can be referenced by {@link VariableRefExpr}s. These no longer operate
 * on strings as identifiers for equality, i.e. two different variables with the same name can exist
 * alongside each other. This allows shadowing
 */
public final class Variable {
    @NotNull private final String name;
    @Nullable private Type type = null;

    public Variable(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the variable after its type has been set using {@link #setType(Type)}
     * Calling this method before the type has been set results in an assertion error
     * @return The type of the value this variable contains
     */
    @NotNull
    public Type getType() {
        assert type != null : "Type of variable has not been declared yet";
        return type;
    }

    /**
     * Set the type of the value this variable will contain
     * @param type The type of the value this variable will contain
     */
    public void setType(@NotNull Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }
}
