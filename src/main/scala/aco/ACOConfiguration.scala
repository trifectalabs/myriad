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
  Q: Double = 4.0,
  terminationCriteria: TerminationCriteria = TerminationCriteria())

