import Main._
import akka.actor._
import WorkerMessages._

abstract class Particle extends Worker {
  override def receive = {
    case UpdateGlobalBest(v: Double) => println(s"Updating my global best value to: $v")
    case _ => 
      println("do shit when they talk")
  }

  override val solution = List[Double]()
}

//Class defining a single particle in the particle swarm optimization algorithm (PSO)

class ParticleImpl extends Particle {
}

