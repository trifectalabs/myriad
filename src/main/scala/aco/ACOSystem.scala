// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package aco

import akka.actor.{ActorRef, ActorSystem}

case class ACOSystem(
  system: ActorSystem,
  nodes: List[ActorRef],
  config: ACOConfiguration)

