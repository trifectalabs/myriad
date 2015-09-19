package com.trifectalabs.myriad
package pso

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PSOExecutor(pso: PSOSystem) extends Executor {
  override def run(): Future[Result] = {
    val particleCount = pso.particles.length

    println(s"\nExecuting job with:")
    println(s"\tParticle count: $particleCount")
    println(s"\tOptimization Function: ${pso.config.objectiveFunction}")
    println(s"\tTermination Criteria: ${pso.config.terminationCriteria}")

    //termination criteria is # of iterations
    for(i <- 1 to 1000) {
      pso.particles.foreach(p => p ! ComputeIteration)
    }
    //wait for actors to process all messages and print results
    Thread.sleep(1000)
    pso.particles.foreach(p => p ! ReportRequest)

    Future{Result(finalValue = 1)}
  }
}
