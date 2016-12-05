package ide;

import backend.errorHandling.Diagnostics;
import backend.parser.Token;
import frontend.JavaDriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Static helper methods for the IDE
 */
enum IDEHelpers {;

    @Nullable
    private static String getStyleNameForToken(@NotNull Token token) {
        switch (token.getKind()) {
            case IDENTIFIER:
                assert token.getPayload() != null;
                switch (token.getPayload()) {
                    case "var":
                    case "out":
                    case "print":
                    case "map":
                    case "reduce":
                        return "keyword";
                    default:
                        return "identifier";
                }
            case ADD:
            case SUB:
            case MULT:
            case DIV:
            case POW:
            case ARROW:
            case COMMA:
            case ASSIGN:
                return "operator";
            case L_PAREN:
            case R_PAREN:
            case L_BRACE:
            case R_BRACE:
                return "braces";
            case INT_LITERAL:
            case FLOAT_LITERAL:
                return "numberLiteral";
            case STRING_LITERAL:
                return "stringLiteral";
            case COMMENT:
                return "comment";
            case ERROR:
            case EOF:
            default:
                return null;
        }
    }

    /**
     * Lex the source code and compute the syntax highlighting based on the lexing result
     * @param sourceCode The source code for which to do syntax highlighting
     * @return The syntax highlighting for the source code
     */
    static List<Highlighting> computeSyntaxHighlighting(@NotNull String sourceCode) {
        List<Highlighting> syntaxHighlighting = new LinkedList<>();

        for (Token token : JavaDriver.lex(sourceCode)) {
            String styleName = getStyleNameForToken(token);
            if (styleName != null) {
                syntaxHighlighting.add(new Highlighting(token.getStartLocation(),
                        token.getEndLocation(), styleName));
            }
        }
        return syntaxHighlighting;
    }

    /**
     * Convert a list of evaluation errors into error highlighting
     * @param errors The list of errors
     * @return The syntax highlighting marking the errors in the source code
     */
    static List<Highlighting> getErrorHighlighting(@NotNull List<Diagnostics.Error> errors) {
        List<Highlighting> errorHighlighting = new LinkedList<>();

        for (Diagnostics.Error error : errors) {
            errorHighlighting.add(new Highlighting(error.getStartLocation(), error.getEndLocation(),
                    "error"));
        }

        return errorHighlighting;
    }
}
