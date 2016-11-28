# RUN: %checkLexer

print "abc"
# CHECK: IDENTIFIER(print)
# CHECK: STRING_LITERAL(abc)