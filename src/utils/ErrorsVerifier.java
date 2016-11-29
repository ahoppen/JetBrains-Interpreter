package utils;

import org.jetbrains.annotations.NotNull;
import parser.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorsVerifier {

    private final Map<Diagnostics.Error, Boolean> expectedErrors = new HashMap<>();

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

            int expectedErrorLine = token.getLocation().getLine();
            if (line != null) {
                expectedErrorLine += Integer.parseInt(line);
            }
            int expectedErrorColumn = Integer.parseInt(column);

            SourceLoc expectedErrorLoc = new SourceLoc(expectedErrorLine, expectedErrorColumn);
            expectedErrors.put(new Diagnostics.Error(expectedErrorLoc, message), false);
        }
    }

    public boolean matchError(Diagnostics.Error error) {
        if (expectedErrors.containsKey(error)) {
            expectedErrors.put(error, true);
            return true;
        } else {
            return false;
        }
    }

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
