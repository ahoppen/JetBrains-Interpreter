package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class IdentifierRefExpr extends Expr {
    @NotNull private final Identifier identifier;

    public IdentifierRefExpr(@NotNull Identifier identifier) {
        this.identifier = identifier;
    }

    @NotNull
    public Identifier getIdentifier() {
        return identifier;
    }

    @Nullable
    @Override
    public Type getType() {
        return identifier.getType();
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitIdentifierRefExpr(this);
    }
}
