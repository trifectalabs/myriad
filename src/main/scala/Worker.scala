package com.trifectalabs.myriad

import akka.actor._

// A worker can be a particle or an ant
trait Worker extends Actor {
  def receive = { case _ => } 
  val solution: List[AnyVal]
}
