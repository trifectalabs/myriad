package com.trifectalabs.myriad

import aco._
import pso._
import akka.actor._

object Main extends App {
  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }
  val r = new scala.util.Random(System.currentTimeMillis)
  val solutions = Seq.fill(9)(List(
      (5.0-(-5.0))*r.nextDouble-5.0, 
      (5.0-(-5.0))*r.nextDouble-5.0
    )
  ).toList
  val conf = PSOConfiguration(
    problemType = ProblemType.Minimization,
    objectiveFunction = obj,
    initialSolutions = solutions
  )
  val psoSystemFactory = new PSOSystemFactory(conf)
  val pso = psoSystemFactory.build()
  val psoJob = new PSOExecutor(pso)
  psoJob.run
}

