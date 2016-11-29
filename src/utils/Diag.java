package utils;

public abstract class Diag {

    // ==========================
    // Lexer
    // ==========================

    public static final String two_dots_in_number_literal = "A number literal can only contain one '.'";
    public static final String unknown_escape_sequence = "Unknown escape sequence '\\%s'";
    public static final String eol_before_string_terminated = "Found end of line before string literal was terminated";
    public static final String eof_before_string_terminated = "Reached end of file before string literal was terminated";
    public static final String invalid_character = "'%s' is an invalid character at this position";

    // ==========================
    // Parser
    // ==========================

    // Statements
    public static final String unexpected_start_of_stmt = "Unexpected start of statements. Statements must start with 'var', 'out' or 'print' but found '%s'";

    // Assign statement
    public static final String expected_ident_after_var = "Expected an identifier after 'var' but found '%s'";
    public static final String expected_equal_sign_in_assignment = "Expected a '=' in an assignment statement but found '%s'";

    // Expression
    public static final String invalid_start_of_expr = "Expected an expression but found '%s'";
    public static final String expected_comma_in_range = "Expected ',' to separate the lower and upper bound of a range but found '%s'";
    public static final String expected_comma_in_map = "Expected ',' to separate argument and lambda in 'map' but found '%s'";
    public static final String expected_comma_in_reduce = "Expected ',' to separate argument and lambda in 'reduce' but found '%s'";
    public static final String expected_comma_between_params_in_reduce = "Expected ',' to separate parameters in 'reduce' but found '%s'";
    public static final String expected_arrow_in_lambda = "Expected '->' to separate parameter and body in lambda but found '%s'";
    public static final String expected_lambda_parameter = "Expected lambda parameter but found '%s'";
    public static final String l_paren_expected = "Expected '(' but found '%s'";
    public static final String r_paren_expected = "Expected ')' but found '%s'";
    public static final String r_brace_expected = "Expected '}' but found '%s'";
}
