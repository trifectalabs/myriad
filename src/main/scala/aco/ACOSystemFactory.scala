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

import akka.actor.{ActorSystem, Props}

class ACOSystemFactory(config: ACOConfiguration) {
  def build(randomSeed: Option[Long] = None,
    actorSystem: Option[ActorSystem] = None
  ): ACOSystem = {
    val system = actorSystem.getOrElse(ActorSystem("AntColonySystem"))
    val placeAgents = (0 until config.numberOfNodes).map(id =>
      system.actorOf(Props(new PlaceAgent(id, config, randomSeed))))
    placeAgents.foreach{a =>
      a ! StartNode(Node(config.start, placeAgents(config.start)))
      a ! FinishNode(Node(config.finish, placeAgents(config.finish)))
    }
    config.paths.foreach{p =>
      placeAgents(p._1) ! NewNeighbour(p._2, placeAgents(p._2))
      if (!config.directedPaths) {
        placeAgents(p._2) ! NewNeighbour(p._1, placeAgents(p._1))
      }
    }
    ACOSystem(system, placeAgents.toList, config)
  }
}
