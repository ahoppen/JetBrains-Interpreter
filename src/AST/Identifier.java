package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Identifier {
    @NotNull private final String name;
    @Nullable private Type type = null;

    public Identifier(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Type getType() {
        return type;
    }

    public void setType(@NotNull Type type) {
        this.type = type;
    }

    public boolean hasType() {
        return type != null;
    }
}
