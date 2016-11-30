package backend.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import backend.errorHandling.Diag;
import backend.errorHandling.Diagnostics;
import backend.utils.SourceLoc;

import java.io.EOFException;
import java.io.Reader;

public class Lexer {

    @NotNull private final Scanner scanner;

    public Lexer(@NotNull Reader inputReader) {
        scanner = new Scanner(inputReader);
    }

    /**
     * The next token in the source code, a token of kind <code>ERROR</code> indicates a lexing
     * error and of kind <code>EOF</code> represents that the end of the file has been reached
     * @return The next token in the source code
     */
    @NotNull
    public Token nextToken() {
        try {
            while (Character.isWhitespace(scanner.peek())) {
                scanner.consume();
            }

            switch (scanner.peek()) {
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H':
                case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P':
                case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
                case 'Y': case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h':
                case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p':
                case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x':
                case 'y': case 'z':
                case '_':
                    return lexIdentifier();
                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
                case '8': case '9': case '.':
                    return lexNumberLiteral(false);
                case '"':
                    return lexStringLiteral();
                case '(':
                    return createSingeCharToken(Token.Kind.L_PAREN);
                case ')':
                    return createSingeCharToken(Token.Kind.R_PAREN);
                case '{':
                    return createSingeCharToken(Token.Kind.L_BRACE);
                case '}':
                    return createSingeCharToken(Token.Kind.R_BRACE);
                case '+':
                    return createSingeCharToken(Token.Kind.ADD);
                case '-': {
                    SourceLoc location = scanner.getCurrentSourceLoc();
                    scanner.consume();
                    switch (scanner.peek()) {
                        case '>':
                            scanner.consume();
                            return new Token(Token.Kind.ARROW, location);
                        case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
                        case '8': case '9': case '.':
                            return lexNumberLiteral(true);
                        default:
                            return new Token(Token.Kind.SUB, location);
                    }
                }
                case '*':
                    return createSingeCharToken(Token.Kind.MULT);
                case '/':
                    return createSingeCharToken(Token.Kind.DIV);
                case '^':
                    return createSingeCharToken(Token.Kind.POW);
                case ',':
                    return createSingeCharToken(Token.Kind.COMMA);
                case '=':
                    return createSingeCharToken(Token.Kind.ASSIGN);
                case '#': {
                    SourceLoc location = scanner.getCurrentSourceLoc();
                    StringBuilder comment = new StringBuilder();
                    try {
                        char nextChar = scanner.consume();
                        // It doesn't matter if we don't consume the newline character(s) since they
                        // will just be consumed as whitespace in the next iteration
                        while (nextChar != '\n' && nextChar != '\r') {
                            comment.append(nextChar);
                            nextChar = scanner.consume();
                        }
                    } catch (EOFException ignored) {}
                    return new Token(Token.Kind.COMMENT, comment.toString(), location);
                }
                default:
                    Diagnostics.error(scanner.getCurrentSourceLoc(), Diag.invalid_character,
                            scanner.peek());
                    return createSingeCharToken(Token.Kind.ERROR);
            }
        } catch (EOFException e) {
            return new Token(Token.Kind.EOF, scanner.getCurrentSourceLoc());
        }
    }

    /**
     * Consumes the next character from the scanner unconditionally and returns a token of the
     * specified kind. The token has no payload and its location is just before the consumed
     * character
     * @param kind The kind of the token to return
     * @return A token of the specified kind
     * @throws EOFException If no character could be consumed from the scanner because the EOF has
     *                      already been reached
     */
    private Token createSingeCharToken(@NotNull Token.Kind kind) throws EOFException {
        SourceLoc location = scanner.getCurrentSourceLoc();
        scanner.consume();
        return new Token(kind, location);
    }

    /**
     * Lex an identifier token. Assumes that the next character is in [A-Za-z0-9_] otherwise an
     * assertion error is thrown.
     * @return The lexed identifier token
     */
    private Token lexIdentifier() {
        SourceLoc location = scanner.getCurrentSourceLoc();
        StringBuilder identifierName = new StringBuilder();
        try {
            characterConsumption: while (true) {
                switch (scanner.peek()) {
                    case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H':
                    case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P':
                    case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
                    case 'Y': case 'Z':
                    case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h':
                    case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p':
                    case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x':
                    case 'y': case 'z':
                    case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
                    case '8': case '9':
                    case '_':
                        identifierName.append(scanner.consume());
                        break;
                    default:
                        break characterConsumption;
                }
            }
        } catch (EOFException ignored) {
        }

        assert identifierName.length() > 0 : "An identifier must contain at least one character";
        return new Token(Token.Kind.IDENTIFIER, identifierName.toString(), location);
    }

    /**
     * Lex a number (int or float) literal. Assumes that the next character is in [0-9.] otherwise
     * an assertion failure occurs.
     * @param negative If the number is preceded by '-' to indicate it's negative
     * @return The lexed number literal or an error token if the next characters did not form a
     *         valid number literal (e.g. '.')
     */
    private Token lexNumberLiteral(boolean negative) {
        SourceLoc location = scanner.getCurrentSourceLoc();
        StringBuilder numberStringBuilder = new StringBuilder();
        if (negative) {
            numberStringBuilder.append("-");
        }
        Token.Kind kind = Token.Kind.INT_LITERAL;
        try {
            characterConsumption: while (true) {
                switch (scanner.peek()) {
                    case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
                    case '8': case '9':
                        numberStringBuilder.append(scanner.consume());
                        break;
                    case '.':
                        if (kind == Token.Kind.INT_LITERAL) {
                            kind = Token.Kind.FLOAT_LITERAL;
                            numberStringBuilder.append(scanner.consume());
                            break;
                        } else {
                            Diagnostics.error(scanner.getCurrentSourceLoc(),
                                    Diag.two_dots_in_number_literal);
                            scanner.consumeCharactersInString("0123456789.");
                            return new Token(Token.Kind.ERROR, location);
                        }
                    default:
                        break characterConsumption;
                }
            }
        } catch (EOFException ignored) {
        }
        String numberString = numberStringBuilder.toString();
        if (numberString.equals(".") || numberString.equals("-.")) {
            Diagnostics.error(location, Diag.single_dot_no_number_literal);
            return new Token(Token.Kind.ERROR, location);
        }
        return new Token(kind, numberStringBuilder.toString(), location);
    }

    /**
     * Lex a string literal, also handling escape sequences. This will do a best effort to always
     * return a string literal, even if there were errors, e.g. by skipping invalid escape sequences
     *
     * This assumes that the next character is a '"' otherwise an assertion failure occurs.
     *
     * @return The lexed string literal
     */
    private Token lexStringLiteral() {
        SourceLoc location = scanner.getCurrentSourceLoc();
        StringBuilder sb = new StringBuilder();
        try {
            char consumedChar = scanner.consume();
            assert consumedChar == '"' : "Haven't we read a quotation mark";

            boolean escapedMode = false;
            SourceLoc lastLocation = scanner.getCurrentSourceLoc();
            char c = scanner.consume();
            do {
                if (escapedMode) {
                    // We have read a '\' before. Parse the escaped character
                    Character escapedCharacter = parseEscapedCharacter(c);
                    if (escapedCharacter != null) {
                        sb.append(escapedCharacter);
                    } else {
                        // The escape sequence wasn't valid, issue an error, skip it and continue
                        Diagnostics.error(lastLocation, Diag.unknown_escape_sequence, c);
                    }
                    escapedMode = false;
                } else {
                    if (c == '\\') {
                        escapedMode = true;
                    } else if (c == '"') {
                        // End of string reached
                        return new Token(Token.Kind.STRING_LITERAL, sb.toString(), location);
                    } else {
                        if (c == '\n' || c == '\r') {
                            // Reached end of line, just assume the string is terminated and
                            // return it
                            Diagnostics.error(location, Diag.eol_before_string_terminated);
                            return new Token(Token.Kind.STRING_LITERAL, sb.toString(), location);
                        }
                        sb.append(c);
                    }
                }

                lastLocation = scanner.getCurrentSourceLoc();
                c = scanner.consume();
            } while (true);
        } catch (EOFException e) {
            Diagnostics.error(location, Diag.eof_before_string_terminated);
            return new Token(Token.Kind.STRING_LITERAL, sb.toString(), location);
        }
    }

    /**
     * @param c The character that follows '\' forming an escape sequence in a string
     * @return The character represented by the escape sequence '\' c or <code>null</code> if the
     * escape sequence was not valid
     */
    @Nullable
    private Character parseEscapedCharacter(char c) {
        if (c == 't') {
            return '\t';
        } else if (c == 'b') {
            return '\b';
        } else if (c == 'n') {
            return '\n';
        } else if (c == 'r') {
            return '\r';
        } else if (c == 'f') {
            return '\f';
        } else if (c == '\'') {
            return '\'';
        } else if (c == '"') {
            return '"';
        } else if (c == '\\') {
            return '\\';
        } else {
            return null;
        }
    }
}
