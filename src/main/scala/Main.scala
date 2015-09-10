import akka.actor._
import WorkerMessages._

object Main extends App {
  val system = ActorSystem("SwarmSystem")
  println("Swarm actor system started.")

  println("Creating a few particles")
  //val particleOne = ParticleImpl()
  val particleOne = system.actorOf(Props[ParticleImpl], name = "particleOne")
  val particleTwo = system.actorOf(Props[ParticleImpl], name = "particleTwo")
  
  particleOne ! UpdateGlobalBest(15.9) 

}
