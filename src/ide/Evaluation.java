package ide;

import backend.errorHandling.Diagnostics;
import backend.interpreter.Value;
import backend.parser.Token;
import backend.utils.SourceLoc;
import frontend.JavaDriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class Evaluation {

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

    static List<Highlighting> computeSyntaxHighlighting(String text) {
        List<Highlighting> syntaxHighlighting = new LinkedList<>();

        for (Token token : JavaDriver.lex(text)) {
            String styleName = getStyleNameForToken(token);
            if (styleName != null) {
                syntaxHighlighting.add(new Highlighting(token.getStartLocation(),
                        token.getEndLocation(), styleName));
            }
        }
        return syntaxHighlighting;
    }

    static List<Highlighting> getErrorHighlighting(@NotNull List<Diagnostics.Error> errors) {
        List<Highlighting> errorHighlighting = new LinkedList<>();

        for (Diagnostics.Error error : errors) {
            errorHighlighting.add(new Highlighting(error.getStartLocation(), error.getEndLocation(),
                    "error"));
        }

        return errorHighlighting;
    }

    static String getResultsAreaText(@NotNull Map<SourceLoc, Value> evaluationResult,
                                     int numberOfLines) {
        StringBuilder resultsText = new StringBuilder();
        int currentLine = 1;

        List<Map.Entry<SourceLoc, Value>> outputs = new ArrayList<>(evaluationResult.entrySet());
        outputs.sort(Comparator.comparing(Map.Entry::getKey));
        for (Map.Entry<SourceLoc, Value> output : outputs) {
            while (currentLine < output.getKey().getLine()) {
                resultsText.append("\n");
                currentLine++;
            }
            String outputString = output.getValue().toString();
            // Replace newlines with their escape sequence since there is only one line to display
            // one statement's output
            outputString = outputString.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
            resultsText.append(outputString);
        }
        while (currentLine <= numberOfLines) {
            resultsText.append("\n");
            currentLine++;
        }

        return resultsText.toString();
    }
}
