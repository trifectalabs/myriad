package com.trifectalabs.myriad
package pso

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._

class PSOFactory(config: PSOConfiguration) extends Executor {
  val particleCount = config.numParticles
  def obj = config.objectiveFunction

  override def run: Future[Result] = {
    // Will move this at some point I think
    val system = ActorSystem("SwarmSystem")
    println("Swarm actor system started.")

    println(s"\nExecuting job with:") 
    println(s"\tParticle count: ${particleCount}") 
    println(s"\tOptimization Function: ${config.objectiveFunction}") 
    println(s"\tTermination Criteria: ${config.terminationCriteria}") 


    val r = new scala.util.Random(System.currentTimeMillis)

    val initialSolutions = (1 to particleCount).zip(Seq.fill(particleCount)(List((5.0-(-5.0))*r.nextDouble-5.0, (5.0-(-5.0))*r.nextDouble-5.0)))
    println(initialSolutions)

    val bestSolution = (initialSolutions.sortWith{case ((_,sol1), (_,sol2)) => obj(sol1) > obj(sol2)}).head._2

    //Create particles from initial solutions
    val particles = initialSolutions.to[Set].map{case (id, solution) => 
      system.actorOf(Props(classOf[Particle], id, List(0.0, 0.0), 0.792, 1.4944, 1.4944, 
    r.nextDouble, r.nextDouble, solution, bestSolution, obj))}
      //Using a full-connected topology
      particles.foreach(p => p ! UpdateNeighbourhood(particles.diff(Set(p))))

      //termination criteria is # of iterations
      for(i <- 1 to 1000) {
        particles.foreach(p => p ! CalculateNewPosition)
      }
      Thread.sleep(1000)
      particles.foreach(p => p ! Report)

    Future{Result(finalValue = 1)}
  }
    
}
