# RUN: %verifyInterpreter

var n = 1000
var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))
var pi = 4 * reduce(sequence, 0, x y -> x + y)
print "pi = "
out pi

# CHECK: pi =
# CHECK: 3.14259