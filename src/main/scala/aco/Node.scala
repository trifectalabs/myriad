// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package aco

import akka.actor.ActorRef

case class Node(
  id: Int,
  ref: ActorRef)

case class Path(
  begin: Node,
  end: Node,
  index: Int,
  pheromone: Double,
  distance: Option[Double] = None)

