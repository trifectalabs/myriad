// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package pso

import akka.actor.{ActorRef, ActorSystem}

case class PSOSystem(
  system: ActorSystem,
  config: PSOConfiguration,
  master: ActorRef,
  particles: List[ActorRef])

