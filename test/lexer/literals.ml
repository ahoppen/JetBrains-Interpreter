# RUN: %lexerVerify

var a = 1.2.3 # expectedError@12: A number literal can only contain one '.'

print "blah\xblub" # expectedError@13: Unknown escape sequence '\x'

# expectedError@+1:7: Found end of line before string literal was terminated
print "unterminated String

# Don't add anything after this string
# expectedError@+1:7: Reached end of file before string literal was terminated
print "unterminated String