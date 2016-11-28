package AST;

import utils.ASTVisitor;

public abstract class ASTNode {

    public abstract <T> T acceptVisitor(ASTVisitor<T> visitor);

}
