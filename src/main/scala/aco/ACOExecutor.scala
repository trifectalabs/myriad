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

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Future

class ACOExecutor(aco: ACOSystem) extends Executor {
  override def run: Future[Result] = {
    (0 until aco.config.numberOfAnts).foreach(a =>
      aco.nodes(aco.config.start) ! AntMessage(a, List(), None, None))

    implicit val timeout = Timeout(5.seconds)
    implicit val ec = aco.system.dispatcher

    val best: Future[Any] =
      aco.nodes(aco.config.finish) ? ResultRequest

    best.map(_ match {
      case ColonyBest(b) => b match {
        case None => Result(Right(List()))
        case Some(s) => Result(Right(s.asInstanceOf[List[Path]]))
      }
      case _ => throw new RuntimeException("Unknown answer type")
    })
  }

  def shutdown: Unit = {
    aco.system.terminate()
  }
}

