# RUN: %verifyLexer

print "abc"
# CHECK: IDENTIFIER(print)
# CHECK: STRING_LITERAL(abc)