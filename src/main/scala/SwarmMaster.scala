// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad

import pso.{TerminationCriteria, ComputeIteration}

import akka.actor.{ActorRef, Actor}

sealed trait SwarmMessage
case object WorkerCheckup extends SwarmMessage
case object SwarmRequest extends SwarmMessage
case object PrintSwarmScore extends SwarmMessage
case object ResultRequest extends SwarmMessage
case class RegisterWorker(worker: ParticleState) extends SwarmMessage
case class WorkerCheckIn(worker: ParticleState) extends SwarmMessage
case class BestRequest(returnRef: ActorRef) extends SwarmMessage
case class SwarmBest(best: Option[List[AnyVal]]) extends SwarmMessage
case class SwarmReport(
  swarmState: Map[ActorRef, WorkerState]) extends SwarmMessage

trait WorkerState {
  val best: List[AnyVal]
  val solution: List[AnyVal]
  val iteration: Int
}
case class ParticleState(
  velocity: List[Double],
  solution: List[Double],
  lbest: List[Double],
  best: List[Double],
  iteration: Int) extends WorkerState
case class AntState(
  solution: List[Int],
  best: List[Int],
  iteration: Int) extends WorkerState

class SwarmMaster(objectiveFunction: List[AnyVal] => Double,
    problemType: ProblemType.Value,
    terminationCriteria: TerminationCriteria) extends Actor {
  var swarmState: Map[ActorRef, WorkerState] = Map()
  var swarmBest: Option[List[AnyVal]] = None
  var isRunning = true

  override def receive: Receive = {
    case RegisterWorker(workerState: WorkerState) =>
      updateWorkerState(workerState)
    case WorkerCheckIn(workerState: WorkerState) =>
      updateWorkerState(workerState)
      if (workerState.iteration != 0 &&
        workerState.iteration < terminationCriteria.maxIterations) {
        sender ! ComputeIteration
      }
      isRunning = swarmState.values.map(s =>
        s.iteration < terminationCriteria.maxIterations).reduceLeft(_ && _)
    case SwarmRequest =>
      sender ! SwarmReport(swarmState)
    case ResultRequest =>
      self ! BestRequest(sender)
    case BestRequest(returnRef: ActorRef) =>
      if (isRunning) self ! BestRequest(returnRef)
      else returnRef ! SwarmBest(swarmBest)
    case _ =>
      println("Unknown message for swarm monitor")
  }

  def updateWorkerState(workerState: WorkerState): Unit = {
    swarmState += (sender -> workerState)
    val update = swarmBest match {
      case None => true
      case Some(best) =>
        ProblemType.compare(
          problemType,
          objectiveFunction(workerState.best),
          objectiveFunction(best))
    }
    if (update) {
      swarmBest = Some(workerState.best)
    }
  }
}

