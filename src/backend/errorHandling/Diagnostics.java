package backend.errorHandling;

import backend.AST.ASTNode;
import org.jetbrains.annotations.NotNull;
import backend.parser.Token;
import backend.utils.SourceLoc;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class Diagnostics {

    public static class Error {

        public static class FixItInsert {
            @NotNull private final SourceLoc location;
            @NotNull private final String toInsert;

            public FixItInsert(@NotNull SourceLoc location, @NotNull String toInsert) {
                this.location = location;
                this.toInsert = toInsert;
            }

            @NotNull
            public SourceLoc getLocation() {
                return location;
            }

            @NotNull
            public String getToInsert() {
                return toInsert;
            }
        }

        @NotNull private final SourceLoc startLocation;
        @NotNull private final SourceLoc endLocation;
        @NotNull private final String message;
        @Nullable private FixItInsert fixIt;

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

        public void fixItInsert(@NotNull SourceLoc location, @NotNull String toInsert) {
            fixIt = new FixItInsert(location, toInsert);
        }

        @Nullable
        public FixItInsert getFixIt() {
            return fixIt;
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
     * @param errorMessage The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public Error error(@NotNull SourceLoc startLocation, @NotNull SourceLoc endLocation,
                      @NotNull String errorMessage, Object... args) {
        errorMessage = String.format(errorMessage, args);
        Error error = new Error(startLocation, endLocation, errorMessage);
        errors.add(error);
        return error;
    }

    /**
     * Report an new error
     * @param token The token at which the error occurred
     * @param errorMessage The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public Error error(@NotNull Token token, @NotNull String errorMessage, Object... args) {
        return error(token.getStartLocation(), token.getEndLocation(), errorMessage, args);
    }

    /**
     * Report an new error
     * @param astNode The AST node where the error occurred
     * @param errorMessage The error message. May contain placeholders for <code>args</code>
     * @param args Objects to be inserted into the error message's placeholders
     */
    public Error error(@NotNull ASTNode astNode, @NotNull String errorMessage, Object... args) {
        return error(astNode.getStartLocation(), astNode.getEndLocation(), errorMessage, args);
    }

    @NotNull
    public List<Error> getErrors() {
        return errors;
    }
}
