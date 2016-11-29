package parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.SourceLoc;

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
        COMMENT
    }

    @NotNull private final Kind kind;
    @Nullable private final String payload;
    @NotNull private final SourceLoc location;

    Token(@NotNull Kind kind, @Nullable String payload, @NotNull SourceLoc location) {
        this.kind = kind;
        this.payload = payload;
        this.location = location;
    }

    Token(Kind kind, @NotNull SourceLoc location) {
        this(kind, null, location);
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
    public SourceLoc getLocation() {
        return location;
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
