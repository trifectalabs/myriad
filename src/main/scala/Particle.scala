import akka.actor._

sealed trait ParticleMessage
case class NeighbourhoodUpdate(neighbours: List[ActorRef]) extends ParticleMessage
case class GlobalBestUpdate(best: TrainingPlan) extends ParticleMessage

//Class defining a single particle in the particle swarm optimization algorithm (PSO)
class Particle extends Actor {
  def receive = {
    case NeighbourhoodUpdate(neighbours) => 
      println("I should update the neighbourhood here")
    case GlobalBestUpdate(best) => 
      println("I should update the global best here")
  }
}
