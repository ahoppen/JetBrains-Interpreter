package backend.AST.Type;

import org.jetbrains.annotations.NotNull;

public final class SequenceType extends Type {

    @NotNull private final Type subType;

    /**
     * @param subType The type of elements this sequence contains
     */
    public SequenceType(@NotNull Type subType) {
        this.subType = subType;
    }

    /**
     * @return The type of elements this sequence contains
     */
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
