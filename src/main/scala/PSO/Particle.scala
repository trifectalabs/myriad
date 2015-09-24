// Copyright (C) 2015 Josiah Witt and Christopher Poenaru
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
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

import akka.actor.{ActorRef, Actor}

sealed trait ParticleMessage
case object ComputeIteration extends ParticleMessage
case class UpdateNeighbourhoodBest(best: List[Double]) extends ParticleMessage
case class UpdateNeighbourhood(
  neighbours: Set[ActorRef]) extends ParticleMessage
case class NewNeighbour(neighbour: ActorRef) extends ParticleMessage
case object ReportRequest extends ParticleMessage
case class Report(vel: List[Double], pos: List[Double],
  lbest: List[Double], nbest: List[Double]) extends ParticleMessage
case object GetNeighbourhood extends ParticleMessage
case class CurrentNeighbourhood(
  neighbourhood: Set[ActorRef]) extends ParticleMessage

class Particle(initVelocity: List[Double], inertia: Double, localAccel: Double,
    neighbourhoodAccel: Double, localR: Double, neighbourhoodR: Double,
    lbest: List[Double], nbest: List[Double], problemType: ProblemType.Value,
    objectiveFunction: List[Double] => Double) extends Actor {
  val w = inertia
  val c1 = localAccel
  val c2 = neighbourhoodAccel
  val r1 = localR
  val r2 = neighbourhoodR
  var currVel = initVelocity
  var currPos = lbest
  var localBest = lbest
  var neighbourhoodBest = nbest
  var neighbourhood = Set[ActorRef]()

  override def receive: Receive = {
    // Update velocity and position then check if bests need to be updated
    // and communicated to neighbours
    case ComputeIteration =>
      computeIteration()
    // Check and update if best received is better than known neighbourhood best
    case UpdateNeighbourhoodBest(best: List[Double]) =>
      recieveNeighbourhoodUpdate(best)
    // Send current neighbourhood back to sender
    case GetNeighbourhood =>
      sender ! CurrentNeighbourhood(neighbourhood)
    // Replace entire neighbourhood
    case UpdateNeighbourhood(neighbours: Set[ActorRef]) =>
      neighbourhood = neighbours
    // Add a neighbour to neighbourhood
    case NewNeighbour(neighbour: ActorRef) =>
      neighbourhood += neighbour
    // Print local and neighbourhood bests to console
    case ReportRequest =>
      sender ! Report(currVel, currPos, localBest, neighbourhoodBest)
      println(s"Particle: lbest = $localBest, nbest = $neighbourhoodBest")
    // Handle unknown messages
    case _ =>
      throw new RuntimeException("Unknown particle message type")
  }

  private def computeIteration() = {
    calculateNewVelocity()
    calculateNewPosition()
    updateLocalBestIfNecessary()
    updateNeighbourhoodBestIfNecessary()
  }

  // Calculate and update new velocity for the particle
  private def calculateNewVelocity() = {
    currVel = currVel.zip(currPos).zip(localBest.zip(neighbourhoodBest)).map {
      case ((v, p), (l, n)) =>
        (w * v) + (c1 * r1 * (l - p)) + (c2 * r2 * (n - p))
      case _ =>
        throw new RuntimeException()
    }
  }

  // Calculate and update new position for the particle
  private def calculateNewPosition() = {
    currPos = currVel.zip(currPos).map {
      case (v, p) =>
        p + v
      case _ =>
        throw new RuntimeException()
    }
  }

  private def updateLocalBestIfNecessary() = {
    val update = ProblemType.compare(
      problemType,
      objectiveFunction(currPos),
      objectiveFunction(localBest)
    )
    if (update) localBest = currPos
  }

  private def updateNeighbourhoodBestIfNecessary() = {
    val update = ProblemType.compare(
      problemType,
      objectiveFunction(currPos),
      objectiveFunction(neighbourhoodBest)
    )
    if (update) {
      neighbourhoodBest = currPos
      neighbourhood.foreach(n => n ! UpdateNeighbourhoodBest(neighbourhoodBest))
    }
  }

  private def recieveNeighbourhoodUpdate(position: List[Double]) = {
    val update = ProblemType.compare(
      problemType,
      objectiveFunction(position),
      objectiveFunction(neighbourhoodBest)
    )
    if (update) neighbourhoodBest = position
  }
}

