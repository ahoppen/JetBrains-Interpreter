package AST;

import AST.Type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTVisitor;

public class BinaryOperatorExpr extends Expr {

    public enum Operator {
        ADD,
        SUB,
        MULT,
        DIV,
        POW
    }

    @NotNull private final Expr lhs;
    @NotNull private final Operator op;
    @NotNull private final Expr rhs;

    public BinaryOperatorExpr(@NotNull Expr lhs, @NotNull Operator op, @NotNull Expr rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @NotNull
    public Expr getLhs() {
        return lhs;
    }

    @NotNull
    public Operator getOp() {
        return op;
    }

    @NotNull
    public Expr getRhs() {
        return rhs;
    }

    @Nullable
    @Override
    public Type getType() {
        return lhs.getType();
    }

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitBinaryOperatorExpr(this);
    }
}
