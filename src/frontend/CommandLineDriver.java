package frontend;

import backend.errorHandling.Diagnostics;
import backend.errorHandling.ErrorsVerifier;
import backend.interpreter.Interpreter;
import backend.interpreter.Value;
import backend.parser.Lexer;
import backend.parser.Parser;
import backend.parser.Token;
import backend.typeChecker.TypeChecker;
import backend.utils.ASTPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineDriver {

    public static void main(String[] args) throws IOException {
        if (args.length != 2 && args.length != 3) {
            printUsage();
            System.exit(1);
        }

        String mode = args[0];
        boolean verify;
        String filename;
        if (args[1].equals("-verify")) {
            verify = true;
            filename = args[2];
        } else {
            verify = false;
            filename = args[1];
        }

        File inputFile = new File(filename);
        FileReader reader;
        try {
            reader = new FileReader(inputFile);
        } catch (FileNotFoundException e) {
            printUsage();
            System.exit(1);
            return;
        }

        Diagnostics diagnostics = new Diagnostics();

        ErrorsVerifier verifier = null;
        OutputStream outputStream = System.out;
        ByteArrayOutputStream byteOutputStream = null;
        if (verify) {
            verifier = new ErrorsVerifier();
            byteOutputStream = new ByteArrayOutputStream();
            outputStream = byteOutputStream;
        }

        switch (mode) {
            case "-lex": {
                lex(reader, diagnostics, verifier, outputStream);
                break;
            }
            case "-parse": {
                parse(reader, diagnostics, verifier, outputStream);
                break;
            }
            case "-typeCheck": {
                typeCheck(reader, diagnostics, verifier, outputStream);
                break;
            }
            case "-evaluate": {
                evaluate(reader, diagnostics, verifier, outputStream);
                break;
            }
            default:
                printUsage();
                System.exit(1);
        }

        verifyOutput(inputFile, byteOutputStream, diagnostics, verifier);
    }

    private static void lex(@NotNull FileReader reader, @NotNull Diagnostics diagnostics,
                            @Nullable ErrorsVerifier verifier,
                            @NotNull OutputStream outputStream) throws IOException {
        Lexer lexer = new Lexer(reader, diagnostics);
        Token token = lexer.nextToken();
        while (token.getKind() !=  Token.Kind.EOF) {
            if (token.getKind() != Token.Kind.COMMENT) {
                outputStream.write(token.toString().getBytes());
                outputStream.write(System.lineSeparator().getBytes());
            }
            if (verifier != null) {
                verifier.addPotentialExpectedError(token);
            }
            token = lexer.nextToken();
        }
    }

    private static void parse(@NotNull FileReader reader, @NotNull Diagnostics diagnostics,
                              @Nullable ErrorsVerifier verifier,
                              @NotNull OutputStream outputStream) {
        ASTPrinter printer = new ASTPrinter(outputStream);
        Parser parser = new Parser(reader, printer, diagnostics, verifier);
        parser.parse();
    }

    private static void typeCheck(@NotNull FileReader reader, @NotNull Diagnostics diagnostics,
                                  @Nullable ErrorsVerifier verifier,
                                  @NotNull OutputStream outputStream) {
        ASTPrinter printer = new ASTPrinter(outputStream);
        TypeChecker typeChecker = new TypeChecker(printer, diagnostics);
        Parser parser = new Parser(reader, typeChecker, diagnostics, verifier);
        parser.parse();
    }

    private static void evaluate(@NotNull FileReader reader, @NotNull Diagnostics diagnostics,
                                 @Nullable ErrorsVerifier verifier,
                                 @NotNull OutputStream outputStream) throws IOException {
        Interpreter interpreter = new Interpreter(diagnostics);
        TypeChecker typeChecker = new TypeChecker(interpreter, diagnostics);
        Parser parser = new Parser(reader, typeChecker, diagnostics, verifier);
        parser.parse();

        for (Value value : interpreter.getOutput().values()) {
            outputStream.write(value.toString().getBytes());
            outputStream.write(System.lineSeparator().getBytes());
        }
    }

    private static void verifyOutput(@NotNull File verifyFile,
                                     @Nullable ByteArrayOutputStream outputStream,
                                     @NotNull Diagnostics diagnostics,
                                     @Nullable ErrorsVerifier verifier) throws IOException {
        Queue<String> checkPatterns = getVerificationStrings(verifyFile);

        if (verifier != null) {
            assert outputStream != null;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(outputStream.toByteArray());
            BufferedReader reader = new BufferedReader(new InputStreamReader(byteStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!checkPatterns.isEmpty() && line.contains(checkPatterns.peek())) {
                    checkPatterns.remove();
                }
                System.out.println(line);
            }
            if (!checkPatterns.isEmpty()) {
                System.err.println("Pattern not found:");
                System.err.println(checkPatterns.peek());
                System.exit(1);
            }
            System.exit(verifyErrors(verifier, diagnostics) ? 0 : 1);
        } else {
            printErrors(diagnostics);
        }
    }

    private static Queue<String> getVerificationStrings(@NotNull File verifyFile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(verifyFile));
        String line;
        Queue<String> checkPatterns = new LinkedList<>();
        while ((line = reader.readLine()) != null) {
            Pattern pattern = Pattern.compile("CHECK:(.*)$");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String checkPattern = matcher.group(1).trim();
                checkPatterns.add(checkPattern);
            }
        }
        return checkPatterns;
    }

    private static boolean verifyErrors(@NotNull ErrorsVerifier verifier,
                                        @NotNull Diagnostics diagnostics) {
        boolean failure = false;
        for (Diagnostics.Error error : diagnostics.getErrors()) {
            if (!verifier.matchError(error)) {
                System.err.println("Unexpected error seen: " + error.getStartLocation() +
                        ": " + error.getMessage());
                failure = true;
            }
        }
        for (Diagnostics.Error error : verifier.getUnseenErrors()) {
            failure = true;
            System.err.println("Expected error not seen: " + error.getStartLocation() +
                    ": " + error.getMessage());
        }
        return !failure;
    }

    private static void printErrors(@NotNull Diagnostics diagnostics) {
        for (Diagnostics.Error error : diagnostics.getErrors()) {
            System.out.println(error.getStartLocation() + " - " + error.getEndLocation() + ": " +
                    error.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java " + CommandLineDriver.class.getSimpleName() +
                " pass [-verify] inputFile");
        System.out.println("pass can be:");
        System.out.println("-lex        Print the input file's tokens");
        System.out.println("-parser     Parse the source code and print its AST");
        System.out.println("-typeCheck  Parse the source code, resolve variables, do basic type");
        System.out.println("            checking and print the AST");
        System.out.println("-evaluate   Evaluate the source code and print its output");
        System.out.println();
        System.out.println("If -verify is specified errors are verified with their descriptions" +
                "in the comments");
    }
}
