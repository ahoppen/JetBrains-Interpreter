package parser;

import AST.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ASTConsumer;
import utils.Diag;
import utils.Diagnostics;
import utils.ErrorsVerifier;

import java.io.Reader;

public class Parser {

    @NotNull private final Lexer lexer;
    @NotNull private final ASTConsumer consumer;
    @Nullable private final ErrorsVerifier verifier;

    public Parser(@NotNull Reader inputReader, @NotNull ASTConsumer consumer) {
        this(inputReader, consumer, null);
    }

    public Parser(@NotNull Reader inputReader, @NotNull ASTConsumer consumer,
                  @Nullable ErrorsVerifier verifier) {
        lexer = new Lexer(inputReader);
        this.consumer = consumer;
        this.verifier = verifier;
    }

    public void parse() {
        Stmt stmt = parseStmt();
        while (stmt != null) {
            consumer.consumeStmt(stmt);
            stmt = parseStmt();
        }
    }

    /**
     * stmt ::= var assignment | 'out' expr | print '"string"'
     * @return The next parsed statement in the source code or <code>null</code> if EOF has been
     *         reached
     */
    @Nullable
    private Stmt parseStmt() {
        while (true) {
            Token token = nextToken();
            if (token == null) {
                return null;
            }
            if (token.getKind() == Token.Kind.IDENTIFIER) {
                assert token.getPayload() != null;
                switch (token.getPayload()) {
                    case "var":
                        AssignStmt assignmentStmt = parseAssignment(token);
                        if (assignmentStmt != null) {
                            return assignmentStmt;
                        } else {
                            // Error has already been reported. Just try parsing a new statement
                            break;
                        }
                    default:
                        Diagnostics.error(token, Diag.unexpected_start_of_stmt,
                                token.toSourceString());
                }
            } else {
                Diagnostics.error(token, Diag.unexpected_start_of_stmt, token.toSourceString());
            }
        }
    }

    /**
     * assignment ::= identifier '=' expr
     * @return A parsed assignment in the source code or <code>null</code> if none could be parsed
     */
    @Nullable
    private AssignStmt parseAssignment(Token lastToken) {

        // Parse identifier
        Token nextToken = nextToken();
        if (nextToken == null) {
            Diagnostics.error(lastToken, Diag.reached_eof_after_var);
            return null;
        }
        if (nextToken.getKind() != Token.Kind.IDENTIFIER) {
            Diagnostics.error(nextToken, Diag.expected_ident_after_var, nextToken.toSourceString());
            return null;
        }
        assert nextToken.getPayload() != null;
        Identifier ident = new Identifier(nextToken.getPayload());

        // Parse '='
        lastToken = nextToken;
        nextToken = nextToken();
        if (nextToken == null) {
            Diagnostics.error(lastToken, Diag.reached_eof_before_equal_sign);
            return null;
        }
        if (nextToken.getKind() != Token.Kind.ASSIGN) {
            Diagnostics.error(nextToken, Diag.expected_equal_sign_in_assignment,
                    nextToken.toSourceString());
            return null;
        }

        // Parse expr
        Expr expr = parseExpr(nextToken);

        if (expr == null) {
            // Parsing the expression failed. Errors have already been reported, just return null
            return null;
        }
        return new AssignStmt(ident, expr);
    }

    /**
     * expr ::= expr op expr | '(' expr ')' | identifier | '{' expr ',' expr '}' | numberLiteral |
     *          'map(' expr ',' var '->' expr ')' | 'reduce(' expr ',' expr ',' var var '->' expr')'
     * @return The next parsed expression or <code>null</code> if the following source code
     *         does not form a valid expression
     */
    @Nullable
    private Expr parseExpr(Token lastToken) {
        Token nextToken = nextToken();
        if (nextToken == null) {
            Diagnostics.error(lastToken, Diag.expected_expr_found_eof);
            return null;
        }
        switch (nextToken.getKind()) {
            case INT_LITERAL:
                assert nextToken.getPayload() != null;
                // We know the token's payload is a valid number
                int value = Integer.parseInt(nextToken.getPayload());
                return new IntLiteralExpr(value);
            default:
                Diagnostics.error(nextToken, Diag.invalid_start_of_expr,
                        nextToken.toSourceString());
                return null;
        }
    }

    /**
     * Returns the next non-comment token in the source file and makes sure to parse expectedError
     * comments using the error verifier if one is specified
     * @return The next non-comment token in the source code
     */
    @Nullable
    private Token nextToken() {
        Token nextToken;
        do {
            nextToken = lexer.nextToken();
            if (verifier != null && nextToken != null) {
                verifier.addPotentialExpectedError(nextToken);
            }
        } while (nextToken != null && nextToken.getKind() == Token.Kind.COMMENT);
        return nextToken;
    }
}
