package utils;

import org.jetbrains.annotations.NotNull;
import parser.Lexer;
import parser.Parser;
import parser.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class CommandLineDriver {

    public static void main(String[] args) {
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

        FileReader reader;
        try {
            reader = new FileReader(new File(filename));
        } catch (FileNotFoundException e) {
            printUsage();
            System.exit(1);
            return;
        }

        switch (mode) {
            case "-lex": {
                Lexer lexer = new Lexer(reader);
                Token token = lexer.nextToken();
                ErrorsVerifier verifier = new ErrorsVerifier();
                while (token != null) {
                    System.out.println(token);
                    if (verify) {
                        verifier.addPotentialExpectedError(token);
                    }
                    token = lexer.nextToken();
                }
                if (verify) {
                    verifyErrors(verifier);
                } else {
                    printErrors();
                }
                break;
            }
            case "-parse": {
                ASTPrinter printer = new ASTPrinter();
                ErrorsVerifier verifier = new ErrorsVerifier();
                Parser parser = new Parser(reader, printer, verifier);
                parser.parse();
                if (verify) {
                    verifyErrors(verifier);
                } else {
                    printErrors();
                }
                break;
            }
            default:
                printUsage();
                System.exit(1);
        }
    }

    private static void verifyErrors(@NotNull ErrorsVerifier verifier) {
        boolean failure = false;
        for (Diagnostics.Error error : Diagnostics.getErrors()) {
            if (!verifier.matchError(error)) {
                System.err.println("Unexpected error seen: " + error.getLocation() +
                        ": " + error.getMessage());
                failure = true;
            }
        }
        for (Diagnostics.Error error : verifier.getUnseenErrors()) {
            failure = true;
            System.err.println("Expected error not seen: " + error.getLocation() +
                    ": " + error.getMessage());
        }
        if (failure) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private static void printErrors() {
        for (Diagnostics.Error error : Diagnostics.getErrors()) {
            System.out.println(error.getLocation() + ": " + error.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java " + CommandLineDriver.class.getSimpleName() +
                " pass [-verify] inputFile");
        System.out.println("pass can be:");
        System.out.println("-lex        Print the input file's tokens");
        System.out.println();
        System.out.println("If -verify is specified errors are verified with their descriptions" +
                "in the comments");
    }
}
