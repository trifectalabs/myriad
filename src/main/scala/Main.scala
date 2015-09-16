package com.trifectalabs.myriad

import aco._
import pso._
import akka.actor._
import WorkerMessages._

object Main extends App {
//  val system = ActorSystem("SwarmSystem")
//  println("Swarm actor system started.")

  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }

  println("Creating a few particles")

  val conf = PSOConfiguration(objectiveFunction = obj) 
  val psoJob = new PSOFactory(conf)
  psoJob.run
}

