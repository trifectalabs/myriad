// Copyright (C) 2015 Josiah Witt and Christopher Poenaru
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
// 02110-1301, USA.

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

