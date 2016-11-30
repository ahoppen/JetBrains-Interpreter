package utils;

import interpreter.Interpreter;
import interpreter.Value;
import org.jetbrains.annotations.NotNull;
import parser.Lexer;
import parser.Parser;
import parser.Token;
import typeChecker.TypeChecker;

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
                while (token.getKind() !=  Token.Kind.EOF) {
                    if (token.getKind() != Token.Kind.COMMENT) {
                        System.out.println(token);
                    }
                    if (verify) {
                        verifier.addPotentialExpectedError(token);
                    }
                    token = lexer.nextToken();
                }
                if (verify) {
                    System.exit(verifyErrors(verifier) ? 0 : 1);
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
                    if (verifyErrors(verifier)) {
                        System.exit(0);
                    } else {
                        System.exit(1);
                    }
                } else {
                    printErrors();
                }
                break;
            }
            case "-typeCheck": {
                ASTPrinter printer = new ASTPrinter();
                TypeChecker typeChecker = new TypeChecker(printer);
                ErrorsVerifier verifier = new ErrorsVerifier();
                Parser parser = new Parser(reader, typeChecker, verifier);
                parser.parse();
                if (verify) {
                    System.exit(verifyErrors(verifier) ? 0 : 1);
                } else {
                    printErrors();
                }
                break;
            }
            case "-evaluate": {
                Interpreter interpreter = new Interpreter();
                TypeChecker typeChecker = new TypeChecker(interpreter);
                ErrorsVerifier verifier = new ErrorsVerifier();
                Parser parser = new Parser(reader, typeChecker, verifier);
                parser.parse();
                boolean failure = false;
                if (verify) {
                    failure = verifyErrors(verifier);
                } else {
                    printErrors();
                }
                for (Value value : interpreter.getOutput().values()) {
                    System.out.print(value);
                }
                System.exit(failure ? 0 : 1);
                break;
            }
            default:
                printUsage();
                System.exit(1);
        }
    }

    private static boolean verifyErrors(@NotNull ErrorsVerifier verifier) {
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
        return !failure;
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
        System.out.println("-parser     Parse the source code and print its AST");
        System.out.println("-typeCheck  Parse the source code, resolve variables, do basic type");
        System.out.println("            checking and print the AST");
        System.out.println("-evaluate   Evaluate the source code and print its output");
        System.out.println();
        System.out.println("If -verify is specified errors are verified with their descriptions" +
                "in the comments");
    }
}
