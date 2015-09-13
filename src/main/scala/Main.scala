import akka.actor._
import WorkerMessages._

object Main extends App {
  val system = ActorSystem("SwarmSystem")
  println("Swarm actor system started.")

  def obj(sol: List[Double]): Double = {
    val x = sol.head
    -x*x + 2.0*x + 11.0
  }

  println("Creating a few particles")
  val r = new scala.util.Random(System.currentTimeMillis)
  //val initialSolutions = Seq.fill(8)((2.0-(-2.0))*r.nextDouble-2.0)
  val initialSolutions = List((0, -1.5), (1, 0.0), (2, 0.5), (3, 1.25))
  val bestSolution = (initialSolutions.sortWith{case ((_,sol1), (_,sol2)) => obj(List(sol1)) > obj(List(sol2))}).head._2
  //Create particles from initial solutions
  val particles = initialSolutions.to[Set].map{case (id, solution) => 
      system.actorOf(Props(classOf[Particle], id, List(0.0), 0.792, 1.4944, 1.4944, 
      r.nextDouble, r.nextDouble, List(solution), List(bestSolution), obj _))}
  //Using a full-connected topology
  particles.foreach(p => p ! UpdateNeighbourhood(particles.diff(Set(p))))
  
  //termination criteria is # of iterations
  for(i <- 1 to 100) {
    particles.foreach(p => p ! CalculateNewPosition)
  }
  Thread.sleep(1000)
  particles.foreach(p => p ! Report)
}

