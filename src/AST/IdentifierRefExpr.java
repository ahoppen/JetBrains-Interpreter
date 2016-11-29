package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;
import utils.SourceLoc;

public class IdentifierRefExpr extends Expr {
    @NotNull private final String identifier;

    public IdentifierRefExpr(@NotNull SourceLoc location, @NotNull String identifier) {
        super(location);
        this.identifier = identifier;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitIdentifierRefExpr(this);
    }
}
