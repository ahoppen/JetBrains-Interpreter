package AST;

import AST.Type.Type;
import org.jetbrains.annotations.Nullable;

public abstract class Expr extends ASTNode {

    @Nullable
    public abstract Type getType();

}
