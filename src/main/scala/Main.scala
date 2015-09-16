import akka.actor._
import WorkerMessages._

object Main extends App {
  val system = ActorSystem("SwarmSystem")
  println("Swarm actor system started.")

  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }

  println("Creating a few particles")
  val r = new scala.util.Random(System.currentTimeMillis)
  val initialSolutions = (1 to 10).zip(Seq.fill(10)(List((5.0-(-5.0))*r.nextDouble-5.0, (5.0-(-5.0))*r.nextDouble-5.0)))
  val bestSolution = (initialSolutions.sortWith{case ((_,sol1), (_,sol2)) => obj(sol1) > obj(sol2)}).head._2
  //Create particles from initial solutions
  val particles = initialSolutions.to[Set].map{case (id, solution) => 
      system.actorOf(Props(classOf[Particle], id, List(0.0, 0.0), 0.792, 1.4944, 1.4944, 
      r.nextDouble, r.nextDouble, solution, bestSolution, obj _))}
  //Using a full-connected topology
  particles.foreach(p => p ! UpdateNeighbourhood(particles.diff(Set(p))))
  
  //termination criteria is # of iterations
  for(i <- 1 to 1000) {
    particles.foreach(p => p ! CalculateNewPosition)
  }
  Thread.sleep(1000)
  particles.foreach(p => p ! Report)
}

