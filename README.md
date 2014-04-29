## Bayesian Knowledge Tracing Parameter Fitting by Simulated Annealing

This code is based on the original [BKT Brute Force](http://www.columbia.edu/~rsb2162/BKT-BruteForce.zip) code from Baker _et al._ (2008).  In that code, BKT parameters were fit by trying every combination of the four BKT parameters `Lzero`, `Guess`, `Slip`, and `T` on a fine-grained grid, and reurning the combination which resulted in the best sum of squared residuals (`SSR`) on predicting the student's answers.

__Note__: _This class expects a tab-delimited input file, sorted by Skill and then by Student -- see the included [TestData.txt](TestData.txt) (the original test file from Baker et al. without modification).  The name of the input file is passed as a command-line argument; e.g._ `java computeKTparams_SA TestData.txt`.

This code modifies that behavior by fitting the parameters using simulated annealing.  Initial, random guesses are selected for the four parameters, and the root mean squared error (`RMSE`) is calculated.  A Monte Carlo algorithm is carried out by ssuccessively randomly changing the parameters by small amounts, and accepting or rejecting these changes based upon the Metropolis criterion:

```java
P(accept) = Math.exp((oldRMSE-newRMSE)/temp)
```

In this equation, `temp` is a virtual temperature, which is slowly decreased until the RMSE stops changing (or until the maximum number of steps, which is set to `1,000,000` by default).  At all times, the set of parameters resulting in the lowest `RMSE` is retained: while the Monte Carlo algorith allows the `RMSE` to increase to avoid getting trapped in local minima, we are interested in the global minimum.

References:  

1.	Baker, R.S.J.d., Corbett, A.T., Aleven, V. (2008) [More Accurate Student Modeling Through Contextual Estimation of Slip and Guess Probabilities in Bayesian Knowledge Tracing](http://dl.acm.org/citation.cfm?id=1426036). _Proceedings of the 9th International Conference on Intelligent Tutoring Systems_, 406-415.
