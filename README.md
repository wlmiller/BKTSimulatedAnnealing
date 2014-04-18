# Bayesian Knowledge Tracing Parameter Fitting by Simulated Annealing

This code is based on the original [BKT Brute Force](http://www.columbia.edu/~rsb2162/BKT-BruteForce.zip) code from Baker _et al._ (2008).  In that code, BKT parameters were fit by trying every combination of the four BKT parameters `Lzero`, `Guess`, `Slip`, and `T` on a fine-grained grid, and reurning the combination which resulted in the best sum of squared residuals (`SSR`) on predicting the student's answers.

This code modifies that behavior by fitting the parameters using simulated annealing.  Initial, random guesses are selected for the four parameters, and the root mean squared error (`RMSE`) is calculated.  They are successively randomly changed by small amounts, and these changes are accepted or rejected based upon the Metropolis criterion on the change in `RMSE`:

```java
P(accept) = Math.exp((oldRMSE-newRMSE)/temp)
```

In this equation, `temp` is a virtual temperature, which is slowly decreased until the RMSE stops changing (or until the maximum number of steps, which is set to `1,000,000` by default).

References:
1. 2.	Baker, R.S.J.d., Corbett, A.T., Aleven, V. (2008) More Accurate Student Modeling Through Contextual Estimation of Slip and Guess Probabilities in Bayesian Knowledge Tracing. _Proceedings of the 9th International Conference on Intelligent Tutoring Systems_, 406-415.
