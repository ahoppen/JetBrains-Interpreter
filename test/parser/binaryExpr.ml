# RUN: %verifyParser

var a = 1 * 2 + 3 * 4
# CHECK: (binaryOperatorExpr op=ADD
# CHECK:   (binaryOperatorExpr op=MULT
# CHECK:     (intLiteralExpr value=1)
# CHECK:     (intLiteralExpr value=2)
# CHECK:   )
# CHECK:   (binaryOperatorExpr op=MULT
# CHECK:     (intLiteralExpr value=3)
# CHECK:     (intLiteralExpr value=4)
# CHECK:   )
# CHECK: )

var b = 3 ^ (4 + 5 * 3)
# CHECK: (binaryOperatorExpr op=POW
# CHECK:   (intLiteralExpr value=3)
# CHECK:   (parenExpr
# CHECK:     (binaryOperatorExpr op=ADD
# CHECK:       (intLiteralExpr value=4)
# CHECK:       (binaryOperatorExpr op=MULT
# CHECK:         (intLiteralExpr value=5)
# CHECK:         (intLiteralExpr value=3)
# CHECK:       )
# CHECK:     )
# CHECK:   )
# CHECK: )