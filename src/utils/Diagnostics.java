package utils;

import org.jetbrains.annotations.NotNull;
import parser.Token;

import java.util.LinkedList;
import java.util.List;

public class Diagnostics {

    public static class Error {
        @NotNull private final SourceLoc location;
        @NotNull private final String message;

        public Error(@NotNull SourceLoc location, @NotNull String message) {
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
        public boolean equals(Object obj) {
            if (!(obj instanceof Error)) {
                return false;
            }
            Error other = ((Error)obj);
            return other.location.equals(location) && other.message.equals(message);
        }

        @Override
        public int hashCode() {
            return location.hashCode() ^ message.hashCode();
        }
    }

    @NotNull private static final List<Error> errors = new LinkedList<>();

    public static void error(@NotNull SourceLoc location, @NotNull String error, Object... args) {
        String errorMessage = String.format(error, args);
        errors.add(new Error(location, errorMessage));
    }

    public static void error(@NotNull Token token, @NotNull String error, Object... args) {
        error(token.getLocation(), error, args);
    }

    @NotNull
    public static List<Error> getErrors() {
        return errors;
    }
}
