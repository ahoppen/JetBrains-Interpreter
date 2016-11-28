package utils;

import parser.Lexer;
import parser.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class CommandLineDriver {

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

        FileReader reader;
        try {
            reader = new FileReader(new File(args[1]));
        } catch (FileNotFoundException e) {
            printUsage();
            System.exit(1);
            return;
        }

        switch (args[0]) {
            case "-lex": {
                Lexer lexer = new Lexer(reader);
                Token token = lexer.nextToken();
                while (token != null) {
                    System.out.println(token);
                    token = lexer.nextToken();
                }
                break;
            }
            default:
                printUsage();
                System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java " + CommandLineDriver.class.getSimpleName() +
                " pass inputFile");
        System.out.println("pass can be:");
        System.out.println("-lex        Print the input file's tokens");
    }
}
