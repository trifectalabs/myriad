// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package example

object SampleFunctions {
  def sphere(X: List[Double]): Double = {
    X.map(x => x * x).sum
  }

  def rosenbrock(X: List[Double]): Double = {
    X.drop(1).zipWithIndex.map{ case (x, i) =>
      100 * math.pow(X(i + 1) - math.pow(x, 2), 2) + math.pow(x - 1, 2)
    }.sum
  }

  def rastrigin(X: List[Double]): Double = {
    X.map(x => 10 + math.pow(x, 2) - 10 * math.cos(2 * math.Pi * x)).sum
  }

  def ackley(X: List[Double]): Double = {
    val squareTerm = X.map(x => x * x).sum
    val cosTerm = X.map(x => math.cos(2 * math.Pi * x)).sum
    val D = X.length // dimensionality of the problem
    -20 * math.exp(-0.2 * math.sqrt(squareTerm / D)) -
      math.exp(cosTerm / D) + 20 + math.E
  }
}

