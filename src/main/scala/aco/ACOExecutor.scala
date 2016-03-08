// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

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

    implicit val timeout = Timeout(10.seconds)
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

