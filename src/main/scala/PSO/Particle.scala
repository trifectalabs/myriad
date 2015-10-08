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

import akka.actor.{ActorRef, Actor}

// TODO: organize all the myriad actor messages
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
    neighbourhoodAccel: Double, lbest: List[Double], nbest: List[Double],
    problemType: ProblemType.Value, objectiveFunction: List[Double] => Double,
    particleSearchSpace: Option[List[(Double, Double)]],
    master: ActorRef, id: Int, randomSeed: Option[Long]) extends Actor {
  val w = inertia
  val c1 = localAccel
  val c2 = neighbourhoodAccel
  val searchSpace = particleSearchSpace.getOrElse(
    lbest.map(_ => (Double.NaN, Double.NaN)))
  val r = new scala.util.Random(randomSeed.getOrElse(System.currentTimeMillis))
  var currVel = initVelocity
  var currPos = lbest
  var localBest = lbest
  var neighbourhoodBest = nbest
  var neighbourhood = Set[ActorRef]()
  var iter = 0
  master ! RegisterWorker(
    ParticleState(currVel, currPos, localBest, neighbourhoodBest, iter))

  override def receive: Receive = {
    // Update velocity and position then check if bests need to be updated
    // and communicated to neighbours
    case ComputeIteration =>
      computeIteration()
    // Check and update if best received is better than known neighbourhood best
    case UpdateNeighbourhoodBest(best: List[Double]) =>
      receiveNeighbourhoodUpdate(best)
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
    // Handle unknown messages
    case _ =>
      throw new RuntimeException("Unknown particle message type")
  }

  def computeIteration(): Unit = {
    iter += 1
    calculateNewVelocity()
    calculateNewPosition()
    updateLocalBestIfNecessary()
    updateNeighbourhoodBestIfNecessary()
    workerCheckIn()
  }

  // Calculate and update new velocity for the particle
  def calculateNewVelocity(): Unit = {
    currVel = currVel.zip(currPos).zip(localBest.zip(neighbourhoodBest)).map {
      case ((v, p), (l, n)) =>
        val r1 = r.nextDouble()
        val r2 = r.nextDouble()
        val vel = (w * v) + (c1 * r1 * (l - p)) + (c2 * r2 * (n - p))
        // TODO: make velocity limit variable
        scala.math.min(vel, 5.0)
      case _ =>
        throw new RuntimeException()
    }
  }

  // Calculate and update new position for the particle
  def calculateNewPosition(): Unit = {
    currPos = currVel.zip(currPos).zip(searchSpace).map {
      case ((v, p), (lowLimit, highLimit)) =>
        val newPos = p + v
        // limit search within the search space
        if (newPos < lowLimit) lowLimit
        else if (newPos > highLimit) highLimit
        else newPos
      case _ =>
        throw new RuntimeException()
    }
  }

  def updateLocalBestIfNecessary(): Unit = {
    val update = ProblemType.compare(
      problemType,
      objectiveFunction(currPos),
      objectiveFunction(localBest))
    if (update) localBest = currPos
  }

  def updateNeighbourhoodBestIfNecessary(): Unit = {
    val update = ProblemType.compare(
      problemType,
      objectiveFunction(currPos),
      objectiveFunction(neighbourhoodBest))
    if (update) {
      neighbourhoodBest = currPos
      // If new neighbourhood best is found, notify neighbours
      neighbourhood.foreach(n => n ! UpdateNeighbourhoodBest(neighbourhoodBest))
    }
  }

  def receiveNeighbourhoodUpdate(position: List[Double]): Unit = {
    // only update newly received new neighbourhood best if a better
    // one hasn't already been found
    val update = ProblemType.compare(
      problemType,
      objectiveFunction(position),
      objectiveFunction(neighbourhoodBest))
    if (update) neighbourhoodBest = position
  }

  def workerCheckIn(): Unit = {
    // Update master on current particle state
    master ! WorkerCheckIn(
      ParticleState(currVel, currPos, localBest, neighbourhoodBest, iter))
  }
}

