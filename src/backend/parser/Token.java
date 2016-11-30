package backend.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import backend.utils.SourceLoc;

/**
 * Represents a token lexed by {@link Lexer}.
 */
public class Token {

    public enum Kind {
        IDENTIFIER,
        ADD,
        SUB,
        MULT,
        DIV,
        POW,
        L_PAREN,
        R_PAREN,
        L_BRACE,
        R_BRACE,
        INT_LITERAL,
        FLOAT_LITERAL,
        ARROW,
        COMMA,
        STRING_LITERAL,
        ASSIGN,
        ERROR,
        COMMENT,
        EOF
    }

    @NotNull private final Kind kind;
    @Nullable private final String payload;
    @NotNull private final SourceLoc startLocation;
    @NotNull private final SourceLoc endLocation;

    Token(@NotNull Kind kind, @Nullable String payload, @NotNull SourceLoc startLocation,
          @NotNull SourceLoc endLocation) {
        this.kind = kind;
        this.payload = payload;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    Token(Kind kind, @NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation) {
        this(kind, null, startLocation, endLocation);
    }

    @NotNull
    public Kind getKind() {
        return kind;
    }

    @Nullable
    public String getPayload() {
        return payload;
    }

    @NotNull
    public SourceLoc getStartLocation() {
        return startLocation;
    }

    @NotNull
    public SourceLoc getEndLocation() {
        return endLocation;
    }

    public boolean isOperator() {
        switch (this.getKind()) {
            case ADD:
            case SUB:
            case MULT:
            case DIV:
            case POW:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        if (payload != null) {
            return kind + "(" + payload + ")";
        } else {
            return kind.toString();
        }
    }

    /**
     * @return A string that is usually used to represent this token in the source code
     */
    @NotNull
    public String toSourceString() {
        switch (getKind()) {
            case IDENTIFIER:
                assert getPayload() != null;
                return getPayload();
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
            case L_PAREN:
                return "(";
            case R_PAREN:
                return ")";
            case L_BRACE:
                return "{";
            case R_BRACE:
                return "}";
            case INT_LITERAL:
                assert getPayload() != null;
                return getPayload();
            case FLOAT_LITERAL:
                assert getPayload() != null;
                return getPayload();
            case ARROW:
                return "->";
            case COMMA:
                return ",";
            case STRING_LITERAL:
                // FIXME: Escape the characters of the payload again
                assert getPayload() != null;
                return "\"" + getPayload() + "\"";
            case ASSIGN:
                return "=";
            case ERROR:
                return "<error>";
            case COMMENT:
                assert getPayload() != null;
                return getPayload();
            case EOF:
                return "EOF";
            default:
                throw new RuntimeException("Unknown token kind: " + getKind());
        }
    }
}
