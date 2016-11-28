package parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.Reader;

public class Lexer {

    @NotNull private final Scanner scanner;

    public Lexer(Reader inputReader) {
        scanner = new Scanner(inputReader);
    }

    @Nullable
    public Token nextToken() {
        try {
            boolean nothingConsumed;
            do {
                nothingConsumed = true;
                // Consume comments and whitespace
                while (Character.isWhitespace(scanner.peek())) {
                    scanner.consume();
                    nothingConsumed = false;
                }

                if (scanner.peek() == '#') {
                    nothingConsumed = false;
                    char nextChar = scanner.consume();
                    while (nextChar != '\n' && nextChar != '\r') {
                        nextChar = scanner.consume();
                    }
                }
            } while (!nothingConsumed);

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
                    return lexNumberLiteral();
                case '"':
                    return lexStringLiteral();
                case '(':
                    scanner.consume();
                    return new Token(Token.Kind.L_PAREN);
                case ')':
                    scanner.consume();
                    return new Token(Token.Kind.R_PAREN);
                case '{':
                    scanner.consume();
                    return new Token(Token.Kind.L_BRACE);
                case '}':
                    scanner.consume();
                    return new Token(Token.Kind.R_BRACE);
                case '+':
                    scanner.consume();
                    return new Token(Token.Kind.ADD);
                case '-':
                    scanner.consume();
                    if (scanner.consumeIf('>')) {
                        return new Token(Token.Kind.ARROW);
                    } else {
                        return new Token(Token.Kind.SUB);
                    }
                case '*':
                    scanner.consume();
                    return new Token(Token.Kind.MULT);
                case '/':
                    scanner.consume();
                    return new Token(Token.Kind.DIV);
                case '^':
                    scanner.consume();
                    return new Token(Token.Kind.POW);
                case ',':
                    scanner.consume();
                    return new Token(Token.Kind.COMMA);
                case '=':
                    scanner.consume();
                    return new Token(Token.Kind.ASSIGN);
                default:
                    // FIXME: Handle invalid characters properly
                    throw new RuntimeException("Unknown character: '" + scanner.peek() + "'");
            }
        } catch (EOFException e) {
            return null;
        }
    }

    private Token lexIdentifier() {
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

        return new Token(Token.Kind.IDENTIFIER, identifierName.toString());
    }

    private Token lexNumberLiteral() {
        StringBuilder numberString = new StringBuilder();
        Token.Kind kind = Token.Kind.INT_LITERAL;
        try {
            characterConsumption: while (true) {
                switch (scanner.peek()) {
                    case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
                    case '8': case '9':
                        numberString.append(scanner.consume());
                        break;
                    case '.':
                        if (kind == Token.Kind.INT_LITERAL) {
                            kind = Token.Kind.FLOAT_LITERAL;
                            numberString.append(scanner.consume());
                            break;
                        } else {
                            // FIXME: Handle two dots in one number properly
                            throw new RuntimeException("Saw two dots in one number literal");
                        }
                    default:
                        break characterConsumption;
                }
            }
        } catch (EOFException ignored) {
        }
        return new Token(kind, numberString.toString());
    }

    private Token lexStringLiteral() {
        try {
            boolean quotConsumed = scanner.consumeIf('"');
            assert quotConsumed : "Haven't we read a quotation mark?";

            StringBuilder sb = new StringBuilder();
            boolean escapedMode = false;
            char c = scanner.consume();
            do {
                if (escapedMode) {
                    Character escapedCharacter = parseEscapedCharacter(c);
                    if (escapedCharacter != null) {
                        sb.append(escapedCharacter);
                    } else {
                        // FIXME: Do proper error diagnostics
                        throw new RuntimeException("Unknown escape sequence");
                    }
                    escapedMode = false;
                } else {
                    if (c == '\\') {
                        escapedMode = true;
                    } else if (c == '"') {
                        return new Token(Token.Kind.STRING_LITERAL, sb.toString());
                    } else {
                        if (c == '\n' || c == '\r') {
                            // FIXME: Do proper error diagnostics
                            throw new RuntimeException("String literal terminated by newline");
                        }
                        sb.append(c);
                    }
                }

                c = scanner.consume();
            } while (true);
        } catch (EOFException e) {
            // FIXME: Do proper error diagnostics
            throw new RuntimeException("Reached EOF before string literal terminated");
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
