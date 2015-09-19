package com.trifectalabs.myriad
package pso

import akka.actor.{ActorRef, ActorSystem}

case class PSOSystem(
  system: ActorSystem,
  config: PSOConfiguration,
  particles: List[ActorRef]
)
