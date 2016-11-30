package parser;

import org.jetbrains.annotations.NotNull;
import utils.SourceLoc;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Allows consumption of characters from the source file and keeps track of the position of the
 * characters in the source file as line and column
 */
class Scanner {

    @NotNull private final Reader inputReader;
    @NotNull private final LinkedList<Character> buffer = new LinkedList<>();
    private int column = 1;
    private int line = 1;

    /**
     * @param inputReader A reader that can be used to read the source code
     */
    Scanner(@NotNull Reader inputReader) {
        this.inputReader = inputReader;
    }

    private void fillBuffer(int size) throws EOFException {
        while (buffer.size() < size) {
            int nextChar;
            try {
                nextChar = inputReader.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (nextChar == -1) {
                throw new EOFException();
            }
            buffer.add((char)nextChar);
        }
    }

    /**
     * @return The next character in the source code without consuming it
     * @throws EOFException If there is no next character in the source code
     */
    char peek() throws EOFException {
        fillBuffer(1);
        return buffer.peek();
    }

    /**
     * Consumes and returns the next character
     * @return The next character in the input stream
     * @throws EOFException If the end of the file has been reached
     */
    char consume() throws EOFException {
        fillBuffer(1);
        char c = buffer.pop();
        if (c == '\n' || c == '\r') {
            // FIXME: Handle \r\n in windows encodings correctly
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    /**
     * Consider the string as a set and consume characters for as long as they are in this set
     * @param s The set of characters that should be consumed
     */
    void consumeCharactersInString(@NotNull String s) {
        Set<Character> chars = new HashSet<>();
        for (char c : s.toCharArray()) {
            chars.add(c);
        }
        try {
            while (chars.contains(peek())) {
                consume();
            }
        } catch (EOFException ignored) {
        }
    }

    /**
     * @return The source location of the next character
     */
    @NotNull
    SourceLoc getCurrentSourceLoc() {
        return new SourceLoc(line, column);
    }
}
