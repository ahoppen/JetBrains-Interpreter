# RUN: %verifyInterpreter

var a = 5
out map({0, 5}, a -> a)
# CHECK: {0, 1, 2, 3, 4, 5}

out map({0, 5}, x -> a * x)
# CHECK: {0, 5, 10, 15, 20, 25}