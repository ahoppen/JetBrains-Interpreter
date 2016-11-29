# RUN: %verifyTypeChecker

var a = 1
var a = 2 # expectedError@1: Variable 'a' has already been declared

var c = b # expectedError@9: Variable 'b' referenced before declaration

var b = b # expectedError@9: Variable 'b' referenced before declaration

var sequence = { 1, 5 }
out sequence * 4 # expectedError@14: Arithmetic operator '*' can only be used on number types and not on 'Sequence<Number>' and 'Number'

var mult = map(sequence, x -> 2 * x)

var x = map(a, x -> 2 * x) # expectedError@13: Argument for 'map' must be a sequence, 'Number' given

var s = { 1.2, 5 }
var t = { 1, 1.2 }

var r = reduce({1, 5}, 0, a b -> a + b)
var r1 = reduce({1, 5}, 0.1, a b -> a + b)
var r2 = reduce(0, 0, a b -> a + b) # expectedError@17: First argument for 'reduce' must be a sequence, 'Number' given

var floatSequence = map({1, 5}, x -> x * 0.1)
var r3 = reduce(floatSequence, 0, a b -> a + b)