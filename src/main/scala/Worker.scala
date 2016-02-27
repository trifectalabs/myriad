// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad

import akka.actor.Actor

// A worker can be a particle or an ant
trait Worker extends Actor {
  def receive: Receive = { case _ => }
  val solution: List[AnyVal]
}

