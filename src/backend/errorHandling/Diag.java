package backend.errorHandling;

/**
 * Collection of error messages. This enum is meant to be uninhabited but only have static error
 * messages
 */
public enum Diag {
    ;

    // ==========================
    // Lexer
    // ==========================

    public static final String two_dots_in_number_literal = "A number literal can only contain one '.'";
    public static final String unknown_escape_sequence = "Unknown escape sequence '\\%s'";
    public static final String eol_before_string_terminated = "Found end of line before string literal was terminated";
    public static final String eof_before_string_terminated = "Reached end of file before string literal was terminated";
    public static final String invalid_character = "'%s' is an invalid character at this position";
    public static final String single_dot_no_number_literal = "A single dot is no number literal";

    // ==========================
    // Parser
    // ==========================

    // Statements
    public static final String unexpected_start_of_stmt = "Unexpected start of statements. Statements must start with 'var', 'out' or 'print' but found '%s'";
    public static final String no_string_literal_after_print = "Expected a string literal after 'print' but got '%s'";

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

    // ==========================
    // Type checker
    // ==========================

    public static final String variable_already_declared = "Variable '%s' has already been declared";
    public static final String undeclared_variable = "Variable '%s' referenced before declaration";
    public static final String arithmetic_operator_on_non_number = "Arithmetic operator '%s' can only be used on number types and not on '%s' and '%s'";
    public static final String argument_of_map_not_sequence = "Argument for 'map' must be a sequence, '%s' given";
    public static final String argument_of_reduce_not_sequence = "First argument for 'reduce' must be a sequence, '%s' given";
    public static final String lambda_of_reduce_does_not_return_base_type = "The lambda of 'reduce' must return the same type as the base element ('%s'). Got '%s'";

    // ==========================
    // Runtime
    // ==========================

    public static final String range_upper_bound_smaller_than_lower_bound = "Upper bound of range cannot be smaller than the lower bound";
    public static final String lower_bound_of_range_not_int = "Lower bound of a range must be an integer and not '%s'";
    public static final String upper_bound_of_range_not_int = "Upper bound of a range must be an integer and not '%s'";
    public static final String division_by_zero = "Division by 0";
}
