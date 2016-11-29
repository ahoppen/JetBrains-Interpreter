package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;
import utils.SourceLoc;

public class IdentifierRefExpr extends Expr {
    @NotNull private final String identifier;
    @Nullable private Variable referencedVariable;

    public IdentifierRefExpr(@NotNull SourceLoc location, @NotNull String identifier) {
        super(location);
        this.identifier = identifier;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public Variable getReferencedVariable() {
        assert referencedVariable != null : "Referenced variable hasn't been set yet";
        return referencedVariable;
    }

    public void setReferencedVariable(@NotNull Variable referencedVariable) {
        this.referencedVariable = referencedVariable;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitIdentifierRefExpr(this);
    }
}
