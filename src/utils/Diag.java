package utils;

public abstract class Diag {

    // Lexer

    public static final String two_dots_in_number_literal = "A number literal can only contain one '.'";
    public static final String unknown_escape_sequence = "Unknown escape sequence '\\%s'";
    public static final String eol_before_string_terminated = "Found end of line before string literal was terminated";
    public static final String eof_before_string_terminated = "Reached end of file before string literal was terminated";
    public static final String invalid_character = "'%s' is an invalid character at this position";
}
