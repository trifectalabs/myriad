// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

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
