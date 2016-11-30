package backend.errorHandling;

import backend.AST.ASTNode;
import org.jetbrains.annotations.NotNull;
import backend.parser.Token;
import backend.utils.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public class Diagnostics {

    public static class Error {
        @NotNull private final SourceLoc location;
        @NotNull private final String message;

        Error(@NotNull SourceLoc location, @NotNull String message) {
            this.location = location;
            this.message = message;
        }

        @NotNull
        public SourceLoc getLocation() {
            return location;
        }

        @NotNull
        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Error)) {
                return false;
            }
            Error error = (Error)o;
            return location.equals(error.location) && message.equals(error.message);
        }

        @Override
        public int hashCode() {
            int result = location.hashCode();
            result = 31 * result + message.hashCode();
            return result;
        }
    }

    @NotNull private static final List<Error> errors = new LinkedList<>();

    /**
     * Report an new error
     * @param location The location where the error occurred
     * @param error The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public static void error(@NotNull SourceLoc location, @NotNull String error, Object... args) {
        String errorMessage = String.format(error, args);
        errors.add(new Error(location, errorMessage));
    }

    /**
     * Report an new error
     * @param token The token at which the error occurred
     * @param error The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public static void error(@NotNull Token token, @NotNull String error, Object... args) {
        error(token.getStartLocation(), error, args);
    }

    /**
     * Report an new error
     * @param astNode The AST node where the error occurred
     * @param error The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public static void error(@NotNull ASTNode astNode, @NotNull String error, Object... args) {
        error(astNode.getLocation(), error, args);
    }

    @NotNull
    public static List<Error> getErrors() {
        return errors;
    }
}
