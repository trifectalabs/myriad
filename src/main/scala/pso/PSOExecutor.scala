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

