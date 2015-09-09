import akka.actor._

object Main extends App {
  val system = ActorSystem("SwarmSystem")
  println("Swarm actor system started.")
}
