// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package pso

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Future

class PSOExecutor(pso: PSOSystem) extends Executor {
  override def run: Future[Result] = {
    // Kick off computation for each particle
    pso.particles.foreach(_ ! ComputeIteration)
    // TODO: make timeout variable depending on length of computation
    implicit val timeout = Timeout(5.seconds)
    implicit val ec = pso.system.dispatcher
    // retrieve the near-optimal value from the swarm master
    val best: Future[Any] = pso.master ? ResultRequest
    // wrap value in Result
    best.map(_ match {
      case SwarmBest(b) => b match {
        case None => Result(Left(List()))
        case Some(s) => Result(Left(s.asInstanceOf[List[Double]]))
      }
      case _ => throw new RuntimeException("Unknown answer type")
    })
  }

  def shutdown: Unit = {
    pso.system.terminate()
  }
}

