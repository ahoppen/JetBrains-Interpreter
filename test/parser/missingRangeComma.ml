# RUN: %verifyParser

var x = {0 5} #expectedError@12: Expected ',' to separate the lower and upper bound of a range but found '5'