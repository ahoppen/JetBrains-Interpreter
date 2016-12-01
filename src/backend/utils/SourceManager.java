package backend.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SourceManager {

    @NotNull private final List<Integer> lineLengths = new ArrayList<>();

    public SourceManager(@NotNull String sourceCode) {
        Reader reader = new StringReader(sourceCode);
        try {
            int currentLineLength = 0;
            int nextChar;
            nextChar = reader.read();
            while (nextChar != -1) {
                currentLineLength++;
                // FIXME: Handle windows line encodings
                if (nextChar == '\n') {
                    lineLengths.add(currentLineLength);
                    currentLineLength = 0;
                }
                nextChar = reader.read();
            }
            if (currentLineLength != 0) {
                lineLengths.add(currentLineLength);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getGlobalOffset(@NotNull SourceLoc location) {
        int offset = 0;
        for (int i = 1; i < location.getLine(); i++) {
            offset += lineLengths.get(i - 1);
        }
        return offset + location.getColumn() - 1;
    }

    /**
     * @return The number of lines in the source code
     */
    public int getNumberOfLines() {
        return lineLengths.size();
    }
}
