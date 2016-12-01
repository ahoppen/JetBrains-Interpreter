package backend.errorHandling;

import backend.AST.ASTNode;
import org.jetbrains.annotations.NotNull;
import backend.parser.Token;
import backend.utils.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public class Diagnostics {

    public static class Error {
        @NotNull private final SourceLoc startLocation;
        @NotNull private final SourceLoc endLocation;
        @NotNull private final String message;

        Error(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
              @NotNull String message) {
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.message = message;
        }

        @NotNull
        public SourceLoc getStartLocation() {
            return startLocation;
        }

        @NotNull
        public SourceLoc getEndLocation() {
            return endLocation;
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
            return startLocation.equals(error.startLocation) && message.equals(error.message);
        }

        @Override
        public int hashCode() {
            int result = startLocation.hashCode();
            result = 31 * result + message.hashCode();
            return result;
        }
    }

    @NotNull private final List<Error> errors = new LinkedList<>();

    /**
     * Report an new error
     * @param startLocation The location where the error started
     * @param endLocation The location where the error ended
     * @param error The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public void error(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                      @NotNull String error, Object... args) {
        String errorMessage = String.format(error, args);
        errors.add(new Error(startLocation, endLocation, errorMessage));
    }

    /**
     * Report an new error
     * @param token The token at which the error occurred
     * @param error The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public void error(@NotNull Token token, @NotNull String error, Object... args) {
        error(token.getStartLocation(), token.getEndLocation(), error, args);
    }

    /**
     * Report an new error
     * @param astNode The AST node where the error occurred
     * @param error The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public void error(@NotNull ASTNode astNode, @NotNull String error, Object... args) {
        error(astNode.getStartLocation(), astNode.getEndLocation(), error, args);
    }

    @NotNull
    public List<Error> getErrors() {
        return errors;
    }
}
