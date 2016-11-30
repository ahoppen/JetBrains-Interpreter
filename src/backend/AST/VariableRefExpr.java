package backend.AST;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * varRefExpr ::= [a-zA-Z_][a-zA-Z0-9_]*
 * </code>
 */
public class VariableRefExpr extends Expr {
    @NotNull private final String variableName;
    @Nullable private Variable referencedVariable;

    public VariableRefExpr(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                           @NotNull String variableName) {
        super(startLocation, endLocation);
        this.variableName = variableName;
    }

    @NotNull
    public String getVariableName() {
        return variableName;
    }

    /**
     * Returns the name of the variable this expression references <b>after</b> it has been set.
     * Calling this method before {@link #setReferencedVariable(Variable)} has been called results
     * in an assertion failure
     * @return The variable this expression references
     */
    @NotNull
    public Variable getReferencedVariable() {
        assert referencedVariable != null : "Referenced variable hasn't been set yet";
        return referencedVariable;
    }

    /**
     * Sets the variable this expression references so that it can be retrieved by
     * {@link #getReferencedVariable()}
     * @param referencedVariable The referenced variable
     */
    public void setReferencedVariable(@NotNull Variable referencedVariable) {
        this.referencedVariable = referencedVariable;
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitIdentifierRefExpr(this);
    }
}
