# RUN: %verifyTypeChecker

var a = 2
var c = map({1, 5}, x -> x * a) # expectedError@30: Variable 'a' referenced before declaration

var d = map({1, 5}, a -> a)