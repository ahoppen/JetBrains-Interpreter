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
    public static final String reached_eof_after_var = "Expected an identifier after 'var' but found nothing";
    public static final String expected_ident_after_var = "Expected an identifier after 'var' but found '%s'";
    public static final String reached_eof_before_equal_sign = "Expected a '=' in an assignment statement but reached end of file";
    public static final String expected_equal_sign_in_assignment = "Expected a '=' in an assignment statement but found '%s'";

    // Expression
    public static final String expected_expr_found_eof = "Expected an expression but found the end of the file";
    public static final String invalid_start_of_expr = "Expected an expression but found '%s'";
}
