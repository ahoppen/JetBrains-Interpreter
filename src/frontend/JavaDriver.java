package frontend;

import backend.AST.Stmt;
import backend.errorHandling.Diagnostics;
import backend.interpreter.Interpreter;
import backend.interpreter.Value;
import backend.parser.Lexer;
import backend.parser.Parser;
import backend.parser.Token;
import backend.typeChecker.TypeChecker;
import backend.utils.SourceLoc;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class JavaDriver {

    public static class EvaluationResult {
        @NotNull private final List<Diagnostics.Error> errors;
        @NotNull private final Map<SourceLoc, Value> output;

        public EvaluationResult(@NotNull List<Diagnostics.Error> errors, @NotNull Map<SourceLoc, Value> output) {
            this.errors = errors;
            this.output = output;
        }

        @NotNull
        public List<Diagnostics.Error> getErrors() {
            return errors;
        }

        @NotNull
        public Map<SourceLoc, Value> getOutput() {
            return output;
        }
    }

    @NotNull
    public static EvaluationResult evaluate(String sourceCode) {
        Reader reader = new StringReader(sourceCode);

        Diagnostics diagnostics = new Diagnostics();

        Interpreter interpreter = new Interpreter(diagnostics);
        TypeChecker typeChecker = new TypeChecker(interpreter, diagnostics);
        Parser parser = new Parser(reader, typeChecker, diagnostics);
        parser.parse();

        Map<SourceLoc, Value> output = new HashMap<>();
        for (Map.Entry<Stmt, Value> entry : interpreter.getOutput().entrySet()) {
            output.put(entry.getKey().getStartLocation(), entry.getValue());
        }

        List<Diagnostics.Error> errors = diagnostics.getErrors();

        return new EvaluationResult(errors, output);
    }

    @NotNull
    public static List<Token> lex(String sourceCode) {
        Reader reader = new StringReader(sourceCode);

        List<Token> tokens = new LinkedList<>();

        Diagnostics diagnostics = new Diagnostics();

        Lexer lexer = new Lexer(reader, diagnostics);
        Token token = lexer.nextToken();
        while (token.getKind() !=  Token.Kind.EOF) {
            tokens.add(token);
            token = lexer.nextToken();
        }

        return tokens;
    }

}
