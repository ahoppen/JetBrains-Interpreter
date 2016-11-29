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
        POW;

        /**
         * Returns the precedence of the operator between 1 and 3. A greater value means higher
         * precedence
         * @return The precedence of this operator
         */
        public int getPrecedence() {
            switch (this) {
                case ADD:
                case SUB:
                    return 1;
                case MULT:
                case DIV:
                    return 2;
                case POW:
                    return 3;
                default:
                    throw new RuntimeException("Unknown operator: " + this);
            }
        }
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
