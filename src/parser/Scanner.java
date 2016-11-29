package parser;

import org.jetbrains.annotations.NotNull;
import utils.SourceLoc;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

class Scanner {

    @NotNull private final Reader inputReader;
    @NotNull private final LinkedList<Character> buffer = new LinkedList<>();
    private int column = 1;
    private int line = 1;

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

    char peek() throws EOFException {
        fillBuffer(1);
        return buffer.peek();
    }

    /**
     * Consumes and returns the next character
     * @return The next character in the input stream
     * @throws EOFException When the end of the file has been reached
     */
    char consume() throws EOFException {
        fillBuffer(1);
        char c = buffer.pop();
        if (c == '\n' || c == '\r') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    boolean consumeIf(char c) throws EOFException {
        fillBuffer(1);
        if (buffer.peek() == c) {
            buffer.pop();
            return true;
        } else {
            return false;
        }
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

    SourceLoc getCurrentSourceLoc() {
        return new SourceLoc(line, column);
    }
}
