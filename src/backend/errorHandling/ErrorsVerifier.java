package backend.errorHandling;

import org.jetbrains.annotations.NotNull;
import backend.parser.Token;
import backend.utils.SourceLoc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle the verification of errors that occurred against errors that are expected to be seen.
 *
 * <p>
 * Expected errors can be specified by special comments of the form
 * <code>
 * '#expectedError@' (lineOffset ':')? column ':' expectedErrorMessage
 * </code>
 * where <code>lineOffset</code> is of the form ([+-][0-9]+).
 * </p>
 *
 * <p>
 * <code>lineOffset</code> can control that the error message is expected to be seen before or
 * after the line that contains the comment. If not specified, it defaults to the same line as the
 * comment. <code>column</code> specifies the column in which the error message is expected to
 * start. <code>expectedErrorMessage</code> then is the error message that is expected to be issued
 * at this position.
 * </p>
 *
 * <p>
 * For example:
 * <pre>
 * var x = x #expectedError@8: Variable 'x' referenced before declaration
 * </pre>
 * </p>
 */
public final class ErrorsVerifier {

    private final Map<Diagnostics.Error, Boolean> expectedErrors = new HashMap<>();

    /**
     * Try to parse the specified token as a comment that specifies an expected error.
     * If the token is not a comment or doesn't adhere to the expected error comment format
     * no action is taken
     * @param token The token that shall be parsed as an expected error marker
     */
    public void addPotentialExpectedError(@NotNull Token token) {
        if (token.getKind() != Token.Kind.COMMENT) {
            return;
        }

        assert token.getPayload() != null;
        Pattern pattern = Pattern.compile("#\\s*expectedError@([+-][0-9]+)?:?([0-9]+):(.+)");
        Matcher matcher = pattern.matcher(token.getPayload());
        if (matcher.find()) {
            String line = matcher.group(1);
            String column = matcher.group(2);
            String message = matcher.group(3).trim();

            int expectedErrorLine = token.getStartLocation().getLine();
            if (line != null) {
                expectedErrorLine += Integer.parseInt(line);
            }
            int expectedErrorColumn = Integer.parseInt(column);

            SourceLoc expectedErrorLoc = new SourceLoc(expectedErrorLine, expectedErrorColumn);
            // We don't care about the error's length when verifying errors
            Diagnostics.Error expectedError = new Diagnostics.Error(expectedErrorLoc,
                    expectedErrorLoc, message);
            expectedErrors.put(expectedError, false);
        }
    }

    /**
     * Tell the verifier that an error has been found during the program's interpretation. This will
     * either mark an expected error as seen or return <code>false</code> to indicate that the
     * error should not have occurred
     * @param error The error that was found during evaluation
     * @return Whether or not the error was expected to be seen
     */
    public boolean matchError(@NotNull Diagnostics.Error error) {
        if (expectedErrors.containsKey(error)) {
            expectedErrors.put(error, true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return The list of errors that were expected to have occurred but weren't seen
     */
    public List<Diagnostics.Error> getUnseenErrors() {
        List<Diagnostics.Error> unseenErrors = new LinkedList<>();
        for (Map.Entry<Diagnostics.Error, Boolean> entry : expectedErrors.entrySet()) {
            if (!entry.getValue()) {
                unseenErrors.add(entry.getKey());
            }
        }
        return unseenErrors;
    }
}
