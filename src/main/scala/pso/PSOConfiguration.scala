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

package com.trifectalabs.myriad
package pso

/** Determines when the algorithm stops and returns a solution */
// TODO: implement logic for non-maxIterations termination criteria
case class TerminationCriteria(
  maxIterations: Int = 1000,
  // If specified, when a solution better than this is found the
  // algorithm will terminate.
  acceptableSolution: Option[Double] = None,
  // If specified, if the objective function has not improved by
  // improvementDelta in improvementIter iterations then the
  // algorithm will terminate. Must be specified together.
  improvementDelta: Option[Double] = None,
  improvementIter: Option[Int] = None)

case class PSOConfiguration(
  objectiveFunction: List[Double] => Double,
  initialSolutions: List[List[Double]],
  initialVelocity: Option[List[Double]] = None,
  searchSpace: Option[List[(Double, Double)]] = None,
  problemType: ProblemType.Value = ProblemType.Maximization,
  topology: ParticleTopology.Value = ParticleTopology.Toroidal,
  terminationCriteria: TerminationCriteria = TerminationCriteria(),
  inertia: Double = 0.792,
  localAccel: Double = 1.4944,
  neighbourhoodAccel: Double = 1.4944)

