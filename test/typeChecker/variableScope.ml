# RUN: %verifyTypeChecker

var a = 2
var c = map({1, 5}, x -> x * a)

var d = map({1, 5}, a -> a)

var x = reduce({0, x}, 0, a b -> a + b) # expectedError@20: Variable 'x' referenced before declaration

var y = y # expectedError@9: Variable 'y' referenced before declaration