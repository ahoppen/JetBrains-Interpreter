package AST;

import AST.Type.Type;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class ErrorExpr extends Expr {
    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitErrorExpr(this);
    }

    @Nullable
    @Override
    public Type getType() {
        return null;
    }
}
