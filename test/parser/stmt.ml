# RUN: %checkParser

1.2 # expectedError@1: Unexpected start of statements. Statements must start with 'var', 'out' or 'print' but found '1.2'
a # expectedError@1: Unexpected start of statements. Statements must start with 'var', 'out' or 'print' but found 'a'

var x = 1
# CHECK: (assignExpr var=x
# CHECK:   (intLiteralExpr value=1)
# CHECK: )

var 1 # expectedError@5: Expected an identifier after 'var' but found '1'

var x 1 # expectedError@7: Expected a '=' in an assignment statement but found '1'

out 1
# CHECK: (outExpr
# CHECK:   (intLiteralExpr value=1)
# CHECK: )

# expectedError@+1:7: Unexpected start of statements. Statements must start with 'var', 'out' or 'print' but found '('
print ( #expectedError@7: Expected a string literal after 'print' but got '('