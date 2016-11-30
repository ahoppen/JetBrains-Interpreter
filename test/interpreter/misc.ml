# RUN: %verifyInterpreter

var s = { 1.2, 5 } # expectedError@11: Lower bound of a range must be an integer and not 'Float'
var t = { 1, 1.2 } # expectedError@14: Upper bound of a range must be an integer and not 'Float'
var u = { 5, 3 } # expectedError@9: Upper bound of range cannot be smaller than the lower bound
var v = { 6, 6 }
out v # CHECK {6}

var r = reduce({1, 5}, 0, a b -> a + b)
var r1 = reduce({1, 5}, 0.1, a b -> a + b)
out r
print "\n" # CHECK: 15
out r1
print "\n" # CHECK: 15.1

var floatSequence = map({1, 5}, x -> x * 0.1)
var r3 = reduce(floatSequence, 0, a b -> a + b)
out r3
print "\n" # CHECK: 1.5