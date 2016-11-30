package parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Diag;
import utils.Diagnostics;
import utils.SourceLoc;

import java.io.EOFException;
import java.io.Reader;

public class Lexer {

    @NotNull private final Scanner scanner;

    public Lexer(@NotNull Reader inputReader) {
        scanner = new Scanner(inputReader);
    }

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

    private Token createSingeCharToken(Token.Kind kind) throws EOFException {
        SourceLoc location = scanner.getCurrentSourceLoc();
        scanner.consume();
        return new Token(kind, location);
    }

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

        return new Token(Token.Kind.IDENTIFIER, identifierName.toString(), location);
    }

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

    private Token lexStringLiteral() {
        SourceLoc location = scanner.getCurrentSourceLoc();
        StringBuilder sb = new StringBuilder();
        try {
            scanner.consume();

            boolean escapedMode = false;
            char c = scanner.consume();
            do {
                if (escapedMode) {
                    Character escapedCharacter = parseEscapedCharacter(c);
                    if (escapedCharacter != null) {
                        sb.append(escapedCharacter);
                    } else {
                        Diagnostics.error(scanner.getCurrentSourceLoc(),
                                Diag.unknown_escape_sequence, c);
                    }
                    escapedMode = false;
                } else {
                    if (c == '\\') {
                        escapedMode = true;
                    } else if (c == '"') {
                        return new Token(Token.Kind.STRING_LITERAL, sb.toString(), location);
                    } else {
                        if (c == '\n' || c == '\r') {
                            Diagnostics.error(location, Diag.eol_before_string_terminated);
                            return new Token(Token.Kind.STRING_LITERAL, sb.toString(), location);
                        }
                        sb.append(c);
                    }
                }

                c = scanner.consume();
            } while (true);
        } catch (EOFException e) {
            Diagnostics.error(location, Diag.eof_before_string_terminated);
            return new Token(Token.Kind.STRING_LITERAL, sb.toString(), location);
        }
    }

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
