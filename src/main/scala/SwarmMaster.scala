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

import akka.actor.{ActorRef, Actor}

sealed trait SwarmMessage
case class WorkerCheckIn(worker: ParticleState) extends SwarmMessage
case object WorkerCheckup extends SwarmMessage
case object SwarmRequest extends SwarmMessage
case class SwarmReport(
  swarmState: Map[ActorRef, WorkerState]
) extends SwarmMessage
case object PrintSwarmScore extends SwarmMessage
trait WorkerState
case class ParticleState(
  velocity: List[Double],
  solution: List[Double],
  lbest: List[Double],
  best: List[Double]
) extends WorkerState
case class AntState(
  solution: List[Int],
  best: List[Int]
) extends WorkerState

class SwarmMaster(objectiveFunction: List[Any] => Double,
    problemType: ProblemType.Value) extends Actor {
  var swarmState: Map[ActorRef, WorkerState] = Map()
  var swarmScore: Option[Double] = None

  override def receive: Receive = {
    case WorkerCheckIn(workerState: WorkerState) =>
      swarmState += (sender -> workerState)
      val update = swarmScore match {
        case None => true
        case Some(score) =>
          ProblemType.compare(
            problemType,
            objectiveFunction(workerState.best),
            score
          )
      }
      if (update) {
        swarmScore = Some(objectiveFunction(workerState.best))
        println(swarmScore)
      }
    case SwarmRequest =>
      sender ! SwarmReport(swarmState)
    case PrintSwarmScore =>
      println(s"Best score found so far:\t$swarmScore")
    case _ =>
      println("Unknown message for swarm monitor")
  }
}
