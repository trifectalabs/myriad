package com.trifectalabs.myriad

import aco._
import pso._
import akka.actor._

object Main extends App {
//  val system = ActorSystem("SwarmSystem")
//  println("Swarm actor system started.")

  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }

  println("Creating a few particles")

  val conf = PSOConfiguration(
    problemType = ProblemType.Minimization,
    numParticles = 10,
    topology = ParticleTopology.FullyConnected,
    objectiveFunction = obj,
    terminationCriteria = TerminationCriteria()
  ) 
  val psoJob = new PSOFactory(conf)
  psoJob.run
}

