package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Variable {
    @NotNull private final String name;
    @Nullable private Type type = null;

    public Variable(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Type getType() {
        assert type != null : "Type of variable has not been declared yet";
        return type;
    }

    public void setType(@NotNull Type type) {
        this.type = type;
    }

    public boolean hasType() {
        return type != null;
    }
}
