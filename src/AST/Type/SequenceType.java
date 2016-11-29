package AST.Type;

import org.jetbrains.annotations.NotNull;

public class SequenceType extends Type {

    @NotNull private final Type subType;

    public SequenceType(@NotNull Type subType) {
        this.subType = subType;
    }

    @NotNull
    public Type getSubType() {
        return subType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SequenceType && subType.equals(((SequenceType)obj).getSubType());
    }

    @Override
    public String toString() {
        return "Sequence<" + subType + ">";
    }
}
