package backend.AST;

import org.jetbrains.annotations.NotNull;
import backend.utils.ASTVisitor;
import backend.utils.SourceLoc;

/**
 * <code>
 * binOpExpr ::= lhs op rhs
 * </code>
 *
 * <p>where <code>lhs</code> and <code>rhs</code> are expressions and <code>op</code> is
 * <code>+ | - | * | / | ^</code></p>
 */
public final class BinaryOperatorExpr extends Expr {

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

        /**
         * @return The string that is used to represent this operator in the source code
         */
        public String toSourceString() {
            switch (this) {
                case ADD:
                    return "+";
                case SUB:
                    return "-";
                case MULT:
                    return "*";
                case DIV:
                    return "/";
                case POW:
                    return "^";
                default:
                    throw new RuntimeException("Unknown operator: " + this);
            }
        }
    }

    @NotNull private final Expr lhs;
    @NotNull private final Operator op;
    @NotNull private final Expr rhs;

    public BinaryOperatorExpr(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                              @NotNull Expr lhs, @NotNull Operator op, @NotNull Expr rhs) {
        super(startLocation, endLocation);
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

    @Override
    public <T> T acceptVisitor(ASTVisitor<T> visitor) {
        return visitor.visitBinaryOperatorExpr(this);
    }
}
