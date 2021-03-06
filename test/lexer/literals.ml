# RUN: %verifyLexer

var a = 1.2.3 # expectedError@12: A number literal can only contain one '.'

var b = -1
# CHECK: IDENTIFIER(b)
# CHECK: ASSIGN
# CHECK: INT_LITERAL(-1)

var c = -1.2
# CHECK: FLOAT_LITERAL(-1.2)

var d = -.2
# CHECK: FLOAT_LITERAL(-.2)

print "blah\xblub" # expectedError@13: Unknown escape sequence '\x'

# expectedError@+1:7: Found end of line before string literal was terminated
print "unterminated String

# Don't add anything after this string
# expectedError@+1:7: Reached end of file before string literal was terminated
print "unterminated String