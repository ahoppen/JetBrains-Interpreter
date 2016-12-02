package backend.parser;

import backend.AST.*;
import backend.errorHandling.Diag;
import backend.errorHandling.Diagnostics;
import backend.errorHandling.ErrorsVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import backend.utils.*;

import java.io.Reader;

public class Parser {

    @NotNull private final Lexer lexer;
    @NotNull private final ASTConsumer consumer;
    @Nullable private final ErrorsVerifier verifier;
    @NotNull private final Diagnostics diagnostics;
    @Nullable private Token peekedToken;

    public Parser(@NotNull Reader inputReader, @NotNull ASTConsumer consumer,
                  @NotNull Diagnostics diagnostics) {
        this(inputReader, consumer, diagnostics, null);
    }

    public Parser(@NotNull Reader inputReader, @NotNull ASTConsumer consumer,
                  @NotNull Diagnostics diagnostics, @Nullable ErrorsVerifier verifier) {
        lexer = new Lexer(inputReader, diagnostics);
        this.consumer = consumer;
        this.diagnostics = diagnostics;
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
            Token token = consumeToken();
            if (token.getKind() == Token.Kind.EOF) {
                return null;
            }
            if (token.getKind() == Token.Kind.IDENTIFIER) {
                assert token.getPayload() != null;
                switch (token.getPayload()) {
                    case "var": {
                        AssignStmt assignmentStmt = parseAssignment(token.getStartLocation());
                        if (assignmentStmt != null) {
                            return assignmentStmt;
                        } else {
                            // Error has already been reported. Just try parsing a new statement
                            break;
                        }
                    }
                    case "out": {
                        Expr expr = parseExpr();
                        if (expr != null) {
                            return new OutStmt(token.getStartLocation(), expr.getEndLocation(),
                                    expr);
                        } else {
                            break;
                        }
                    }
                    case "print": {
                        Token stringToken = peekToken();
                        if (stringToken.getKind() != Token.Kind.STRING_LITERAL) {
                            diagnostics.error(stringToken, Diag.no_string_literal_after_print,
                                    stringToken);
                            break;
                        }
                        // Consume the string token
                        consumeToken();
                        assert stringToken.getPayload() != null;
                        return new PrintStmt(token.getStartLocation(), stringToken.getEndLocation(),
                                stringToken.getPayload());
                    }
                    default:
                        diagnostics.error(token, Diag.unexpected_start_of_stmt,
                                token.toSourceString());
                }
            } else {
                diagnostics.error(token, Diag.unexpected_start_of_stmt, token.toSourceString());
            }
        }
    }

    private Variable parseVariable(String diag) {
        Token nextToken = consumeToken();
        if (nextToken.getKind() != Token.Kind.IDENTIFIER) {
            diagnostics.error(nextToken, diag, nextToken.toSourceString());
            return null;
        }
        assert nextToken.getPayload() != null;
        return new Variable(nextToken.getPayload());
    }

    /**
     * assignment ::= identifier '=' expr
     * @return A parsed assignment in the source code or <code>null</code> if none could be parsed
     */
    @Nullable
    private AssignStmt parseAssignment(@NotNull SourceLoc location) {
        // Parse identifier
        Variable variable = parseVariable(Diag.expected_ident_after_var);
        if (variable == null) {
            return null;
        }

        // Parse '='
        Token nextToken = consumeToken();
        if (nextToken.getKind() != Token.Kind.ASSIGN) {
            diagnostics.error(nextToken, Diag.expected_equal_sign_in_assignment,
                    nextToken.toSourceString());
            return null;
        }

        // Parse expr
        Expr expr = parseExpr();

        if (expr == null) {
            // Parsing the expression failed. Errors have already been reported, just return null
            return null;
        }
        return new AssignStmt(location, expr.getEndLocation(), variable, expr);
    }

    private BinaryOperatorExpr.Operator getOperatorForToken(Token token) {
        switch (token.getKind()) {
            case ADD:
                return BinaryOperatorExpr.Operator.ADD;
            case SUB:
                return BinaryOperatorExpr.Operator.SUB;
            case MULT:
                return BinaryOperatorExpr.Operator.MULT;
            case DIV:
                return BinaryOperatorExpr.Operator.DIV;
            case POW:
                return BinaryOperatorExpr.Operator.POW;
            default:
                throw new RuntimeException(token + " is no operator");
        }
    }

    /**
     * expr ::= expr op expr | '(' expr ')' | identifier | '{' expr ',' expr '}' | numberLiteral |
     *          'map' map |
     *          'reduce' reduce
     * @return The next parsed expression or <code>null</code> if the following source code
     *         does not form a valid expression
     */
    @Nullable
    private Expr parseExpr() {
        return parseExprImpl(0);
    }

    private Expr parseExprImpl(int precedenceHigherThan) {
        Expr workingExpr = null;
        while (true) {
            Token nextToken = peekToken();
            if (workingExpr == null) {
                workingExpr = parseBaseExpr();
                if (workingExpr == null) {
                    return null;
                }
            } else {
                if (nextToken.isOperator()) {
                    BinaryOperatorExpr.Operator operator = getOperatorForToken(nextToken);
                    if (operator.getPrecedence() <= precedenceHigherThan) {
                        break;
                    }
                    consumeToken();
                    Expr rhs = parseExprImpl(operator.getPrecedence());
                    if (rhs == null) {
                        return null;
                    }
                    workingExpr = new BinaryOperatorExpr(nextToken.getStartLocation(),
                            nextToken.getEndLocation(), workingExpr, operator, rhs);
                } else {
                    break;
                }
            }
        }
        return workingExpr;
    }

    /**
     * Parses an expression without any binary operators. Returns the parsed expression or
     * <code>null</code> if parsing failed
     * @return A base expression or <code>null</code> if parsing failed
     */
    private Expr parseBaseExpr() {
        Token nextToken = peekToken();
        switch (nextToken.getKind()) {
            case INT_LITERAL: {
                consumeToken();
                assert nextToken.getPayload() != null;
                // We know the token's payload is a valid number
                int value = Integer.parseInt(nextToken.getPayload());
                return new IntLiteralExpr(nextToken.getStartLocation(), nextToken.getEndLocation(),
                        value);
            }
            case FLOAT_LITERAL: {
                consumeToken();
                assert nextToken.getPayload() != null;
                // We know the token's payload is a valid number
                double value = Double.parseDouble(nextToken.getPayload());
                return new FloatLiteralExpr(nextToken.getStartLocation(),
                        nextToken.getEndLocation(), value);
            }
            case L_PAREN: {
                consumeToken();
                Expr subExpr = parseExpr();
                if (subExpr == null) {
                    return null;
                }
                Token rParen = peekToken();
                if (!consumeToken(Token.Kind.R_PAREN, Diag.r_paren_expected, ")")) {
                    return null;
                }
                return new ParenExpr(nextToken.getStartLocation(), rParen.getEndLocation(),
                        subExpr);
            }
            case L_BRACE: {
                consumeToken();
                Expr lowerBound = parseExpr();
                if (lowerBound == null) {
                    return null;
                }
                consumeToken(Token.Kind.COMMA, Diag.expected_comma_in_range, ", ");
                Expr upperBound = parseExpr();
                if (upperBound == null) {
                    return null;
                }
                Token rBrace = consumeToken();
                if (rBrace.getKind() != Token.Kind.R_BRACE) {
                    diagnostics.error(rBrace, Diag.r_brace_expected, rBrace);
                    return null;
                }
                return new RangeExpr(nextToken.getStartLocation(), rBrace.getEndLocation(),
                        lowerBound, upperBound);
            }
            case IDENTIFIER: {
                consumeToken();
                assert nextToken.getPayload() != null;
                switch (nextToken.getPayload()) {
                    case "map":
                        return parseMapExpr(nextToken.getStartLocation());
                    case "reduce":
                        return parseReduceExpr(nextToken.getStartLocation());
                    default:
                        return new VariableRefExpr(nextToken.getStartLocation(),
                                nextToken.getEndLocation(), nextToken.getPayload());
                }
            }
            default: {
                consumeToken();
                diagnostics.error(nextToken, Diag.invalid_start_of_expr,
                        nextToken.toSourceString());
                return null;
            }
        }
    }

    /**
     * map ::= '(' expr ',' var '->' expr ')'
     *
     * Parse the body of a map expression without the map keyword. Either return the successfully
     * parsed expression or <code>null</code> if parsing failed
     * @return A parsed map expression or <code>null</code> if the expression could not be parsed
     */
    private MapExpr parseMapExpr(@NotNull SourceLoc location) {
        // '('
        consumeToken(Token.Kind.L_PAREN, Diag.l_paren_expected, "(");
        // expr
        Expr argument = parseExpr();
        if (argument == null) {
            return null;
        }
        // ','
        consumeToken(Token.Kind.COMMA, Diag.expected_comma_in_map, ", ");
        // var
        Variable param = parseVariable(Diag.expected_lambda_parameter);
        if (param == null) {
            return null;
        }
        // '->'
        if (!consumeToken(Token.Kind.ARROW, Diag.expected_arrow_in_lambda, "->")) {
            return null;
        }
        // expr
        Expr lambda = parseExpr();
        if (lambda == null) {
            return null;
        }
        // ')'
        Token rParen = peekToken();
        consumeToken(Token.Kind.R_PAREN, Diag.r_paren_expected, ")");
        return new MapExpr(location, rParen.getEndLocation(), argument, param, lambda);
    }

    /**
     * reduce ::= '(' expr ',' expr ',' var var '->' expr ')'
     * Parse the body of a reduce expression without the reduce keyword. Either return the
     * successfully parsed expression or <code>null</code> if parsing failed
     * @return A parsed reduce expression or <code>null</code> if the expression could not be parsed
     */
    private ReduceExpr parseReduceExpr(@NotNull SourceLoc location) {
        // '('
        consumeToken(Token.Kind.L_PAREN, Diag.l_paren_expected, "(");
        // expr
        Expr sequence = parseExpr();
        if (sequence == null) {
            return null;
        }
        // ','
        consumeToken(Token.Kind.COMMA, Diag.expected_comma_in_reduce, ", ");
        // expr
        Expr base = parseExpr();
        if (base == null) {
            return null;
        }
        // ','
        consumeToken(Token.Kind.COMMA, Diag.expected_comma_between_params_in_reduce, ", ");
        // var
        Variable lambdaParam1 = parseVariable(Diag.expected_lambda_parameter);
        if (lambdaParam1 == null) {
            return null;
        }
        // var
        Variable lambdaParam2 = parseVariable(Diag.expected_lambda_parameter);
        if (lambdaParam2 == null) {
            return null;
        }
        // '->'
        if (!consumeToken(Token.Kind.ARROW, Diag.expected_arrow_in_lambda, "->")) {
            return null;
        }
        // expr
        Expr lambda = parseExpr();
        if (lambda == null) {
            return null;
        }
        // ')'
        Token rParen = peekToken();
        consumeToken(Token.Kind.R_PAREN, Diag.r_paren_expected, ")");
        return new ReduceExpr(location, rParen.getEndLocation(), base, sequence, lambdaParam1,
                lambdaParam2, lambda);
    }

    /**
     * Check if the next token matches the specified kind. If yes, return <code>true</code> and
     * consume it, otherwise, issue the specified diagnostic and return <code>false</code>
     *
     * The diagnostics is expected to have one placeholder that will carry the token that was
     * actually seen
     *
     * If <code>fixItInsert</code> is not <code>null</code> the diagnostic will offer the user to
     * insert the specified string right in front of the current token
     *
     * @param kind The expected kind of the next token
     * @param diag The diagnostic to issue when a different token was seen
     * @param fixItInsert A string to insert in front of the next token to fix the issue
     * @return <code>true</code> if the specified token was seen, <code>false</code> otherwise
     */
    private boolean consumeToken(Token.Kind kind, @NotNull String diag,
                                 @Nullable String fixItInsert) {
        if (peekToken().getKind() != kind) {
            Diagnostics.Error error = diagnostics.error(peekToken(), diag,
                    peekToken().toSourceString());
            if (fixItInsert != null) {
                error.fixItInsert(peekToken().getStartLocation(), fixItInsert);
            }
            return false;
        } else {
            consumeToken();
            return true;
        }
    }

    /**
     * Returns the next non-comment token in the source file and makes sure to parse expectedError
     * comments using the error verifier if one is specified
     * @return The next non-comment token in the source code
     */
    @NotNull
    private Token consumeToken() {
        if (peekedToken != null) {
            Token nextToken = peekedToken;
            peekedToken = null;
            return nextToken;
        } else {
            Token nextToken;
            do {
                nextToken = lexer.nextToken();
                if (verifier != null) {
                    verifier.addPotentialExpectedError(nextToken);
                }
            } while (nextToken.getKind() == Token.Kind.COMMENT);
            return nextToken;
        }
    }

    private Token peekToken() {
        if (peekedToken != null) {
            return peekedToken;
        } else {
            peekedToken = consumeToken();
            return peekedToken;
        }
    }
}
