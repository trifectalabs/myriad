// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

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

