// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package aco

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

case class ACOConfiguration(
  distanceFunction: (Int, Int, Int, List[Path]) => Double,
  numberOfAnts: Int,
  numberOfNodes: Int,
  paths: List[(Int, Int)],
  start: Int = 0,
  finish: Int = 0,
  directedPaths: Boolean = false,
  multiPathCollapse: Boolean = false,
  alpha: Double = 1.0,
  beta: Double = 1.0,
  Q: Double = 1.0,
  pheromoneDecayRate: Double = 0.1,
  terminationCriteria: TerminationCriteria = TerminationCriteria())

