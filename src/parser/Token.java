package parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        ASSIGN
    }

    @NotNull private final Kind kind;
    @Nullable private final String payload;

    Token(@NotNull Kind kind, @Nullable String payload) {
        this.kind = kind;
        this.payload = payload;
    }

    Token(Kind kind) {
        this(kind, null);
    }

    @NotNull
    public Kind getKind() {
        return kind;
    }

    @Nullable
    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        if (payload != null) {
            return kind + "(" + payload + ")";
        } else {
            return kind.toString();
        }
    }
}
