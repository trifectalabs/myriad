package com.trifectalabs.myriad

/** Determines whether the objective function is maximized
  *  or minimized.
  */
object ProblemType extends Enumeration {
  type ProblemType = Value
  val Minimization, Maximization = Value

  //Abtracted objective function comparison based on problem type
  def compare(problemType: ProblemType.Value, challenger: Double, best: Double): Boolean = {
    if (problemType == ProblemType.Minimization) {
      challenger < best
    } else {
      challenger > best
    }
  }
}