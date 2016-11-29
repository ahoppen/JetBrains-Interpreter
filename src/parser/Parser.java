package parser;

import AST.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.*;

import java.io.Reader;

public class Parser {

    @NotNull private final Lexer lexer;
    @NotNull private final ASTConsumer consumer;
    @Nullable private final ErrorsVerifier verifier;
    @Nullable private Token peekedToken;

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
            Token token = consumeToken();
            if (token.getKind() == Token.Kind.EOF) {
                return null;
            }
            if (token.getKind() == Token.Kind.IDENTIFIER) {
                assert token.getPayload() != null;
                switch (token.getPayload()) {
                    case "var": {
                        AssignStmt assignmentStmt = parseAssignment(token.getLocation());
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
                            return new OutStmt(token.getLocation(), expr);
                        } else {
                            break;
                        }
                    }
                    case "print": {
                        Token stringToken = peekToken();
                        if (stringToken.getKind() != Token.Kind.STRING_LITERAL) {
                            Diagnostics.error(stringToken, Diag.no_string_literal_after_print,
                                    stringToken);
                            break;
                        }
                        // Consume the string token
                        consumeToken();
                        assert stringToken.getPayload() != null;
                        return new PrintStmt(token.getLocation(), stringToken.getPayload());
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

    private Variable parseVariable(String diag) {
        Token nextToken = consumeToken();
        if (nextToken.getKind() != Token.Kind.IDENTIFIER) {
            Diagnostics.error(nextToken, diag, nextToken.toSourceString());
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
            Diagnostics.error(nextToken, Diag.expected_equal_sign_in_assignment,
                    nextToken.toSourceString());
            return null;
        }

        // Parse expr
        Expr expr = parseExpr();

        if (expr == null) {
            // Parsing the expression failed. Errors have already been reported, just return null
            return null;
        }
        return new AssignStmt(location, variable, expr);
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
                    workingExpr = new BinaryOperatorExpr(nextToken.getLocation(), workingExpr,
                            operator, rhs);
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
                return new IntLiteralExpr(nextToken.getLocation(), value);
            }
            case FLOAT_LITERAL: {
                consumeToken();
                assert nextToken.getPayload() != null;
                // We know the token's payload is a valid number
                double value = Double.parseDouble(nextToken.getPayload());
                return new FloatLiteralExpr(nextToken.getLocation(), value);
            }
            case L_PAREN: {
                consumeToken();
                Expr subExpr = parseExpr();
                if (subExpr == null) {
                    return null;
                }
                if (!consumeToken(Token.Kind.R_PAREN, Diag.r_paren_expected)) {
                    return null;
                }
                return new ParenExpr(nextToken.getLocation(), subExpr);
            }
            case L_BRACE: {
                consumeToken();
                Expr lowerBound = parseExpr();
                if (lowerBound == null) {
                    return null;
                }
                if (!consumeToken(Token.Kind.COMMA, Diag.expected_comma_in_range)) {
                    return null;
                }
                Expr upperBound = parseExpr();
                if (upperBound == null) {
                    return null;
                }
                if (!consumeToken(Token.Kind.R_BRACE, Diag.r_brace_expected)) {
                    return null;
                }
                return new RangeExpr(nextToken.getLocation(), lowerBound, upperBound);
            }
            case IDENTIFIER: {
                consumeToken();
                assert nextToken.getPayload() != null;
                switch (nextToken.getPayload()) {
                    case "map":
                        return parseMapExpr(nextToken.getLocation());
                    case "reduce":
                        return parseReduceExpr(nextToken.getLocation());
                    default:
                        return new IdentifierRefExpr(nextToken.getLocation(),
                                nextToken.getPayload());
                }
            }
            default: {
                consumeToken();
                Diagnostics.error(nextToken, Diag.invalid_start_of_expr,
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
        if (!consumeToken(Token.Kind.L_PAREN, Diag.l_paren_expected)) {
            return null;
        }
        // expr
        Expr argument = parseExpr();
        if (argument == null) {
            return null;
        }
        // ','
        if (!consumeToken(Token.Kind.COMMA, Diag.expected_comma_in_map)) {
            return null;
        }
        // var
        Variable param = parseVariable(Diag.expected_lambda_parameter);
        if (param == null) {
            return null;
        }
        // '->'
        if (!consumeToken(Token.Kind.ARROW, Diag.expected_arrow_in_lambda)) {
            return null;
        }
        // expr
        Expr lambda = parseExpr();
        if (lambda == null) {
            return null;
        }
        // ')'
        if (!consumeToken(Token.Kind.R_PAREN, Diag.r_paren_expected)) {
            return null;
        }
        return new MapExpr(location, argument, param, lambda);
    }

    /**
     * reduce ::= '(' expr ',' expr ',' var var '->' expr ')'
     * Parse the body of a reduce expression without the reduce keyword. Either return the
     * successfully parsed expression or <code>null</code> if parsing failed
     * @return A parsed reduce expression or <code>null</code> if the expression could not be parsed
     */
    private ReduceExpr parseReduceExpr(@NotNull SourceLoc location) {
        // '('
        if (!consumeToken(Token.Kind.L_PAREN, Diag.l_paren_expected)) {
            return null;
        }
        // expr
        Expr sequence = parseExpr();
        if (sequence == null) {
            return null;
        }
        // ','
        if (!consumeToken(Token.Kind.COMMA, Diag.expected_comma_in_reduce)) {
            return null;
        }
        // expr
        Expr base = parseExpr();
        if (base == null) {
            return null;
        }
        // ','
        if (!consumeToken(Token.Kind.COMMA, Diag.expected_comma_between_params_in_reduce)) {
            return null;
        }
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
        if (!consumeToken(Token.Kind.ARROW, Diag.expected_arrow_in_lambda)) {
            return null;
        }
        // expr
        Expr lambda = parseExpr();
        if (lambda == null) {
            return null;
        }
        // ')'
        if (!consumeToken(Token.Kind.R_PAREN, Diag.r_paren_expected)) {
            return null;
        }
        return new ReduceExpr(location, base, sequence, lambdaParam1, lambdaParam2, lambda);
    }

    /**
     * Consume the next token, check if it matches the specified kind. If yes, return
     * <code>true</code> otherwise, issue the specified diagnostic and return <code>false</code>
     *
     * The diagnostics is expected to have one placeholder that will carry the token that was
     * actually seen
     * @param kind The expected kind of the next token
     * @param diag The diagnostic to issue when a different token was seen
     * @return <code>true</code> if the specified token was seen, <code>false</code> otherwise
     */
    private boolean consumeToken(Token.Kind kind, String diag) {
        Token nextToken = consumeToken();
        if (nextToken.getKind() != kind) {
            Diagnostics.error(nextToken, diag, nextToken.toSourceString());
            return false;
        } else {
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
