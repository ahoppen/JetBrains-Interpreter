# RUN: %verifyParser

var a = 1
# CHECK: (intLiteralExpr value=1)

var b = 5.2
# CHECK: (floatLiteralExpr value=5.2)

var c = (2)
# CHECK: (parenExpr
# CHECK:   (intLiteralExpr value=2)

var d = { 5, 8 }
# CHECK: (rangeExpr
# CHECK:   (intLiteralExpr value=5)
# CHECK:   (intLiteralExpr value=8)

var e = c
# CHECK: (variableRefExpr identifier=c)

var f = map({1, 5}, x -> 4)
# CHECK: (mapExpr param=x
# CHECK:   (rangeExpr
# CHECK:     (intLiteralExpr value=1)
# CHECK:     (intLiteralExpr value=5)
# CHECK:   )
# CHECK:   (intLiteralExpr value=4)
# CHECK: )

var g = reduce({2, 10}, 0, x y -> y)
# CHECK: (reduceExpr param1=x param2=y
# CHECK:   (intLiteralExpr value=0)
# CHECK:   (rangeExpr
# CHECK:     (intLiteralExpr value=2)
# CHECK:     (intLiteralExpr value=10)
# CHECK:   )
# CHECK:   (variableRefExpr identifier=y)
# CHECK: )