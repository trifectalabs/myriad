import Main._
import akka.actor._

sealed trait ParticleMessage
case object CalculateNewPosition extends ParticleMessage
case class UpdateGlobalBest(best: List[Double]) extends ParticleMessage
case class UpdateNeighbourhood(neighbours: Set[ActorRef]) extends ParticleMessage

class Particle(initVelocity: List[Double], inertiaW: Double, accelCoeff1: Double, 
    accelCoeff2: Double, randCoeff1: Double, randCoeff2: Double, lbest: List[Double], 
    gbest: List[Double], objectiveFunction: List[Double] => Double) extends Actor {
  val w = inertiaW
  val c1 = accelCoeff1
  val c2 = accelCoeff2
  val r1 = randCoeff1
  val r2 = randCoeff2
  var currVel = initVelocity
  var currPos = lbest
  var localBest = lbest
  var globalBest = gbest
  var neighbourhood = Set[ActorRef]()
  
  override def receive = {
    case CalculateNewPosition =>
      println("Calculating new postion")
      currVel = UpdateVelocity()
      currPos = UpdatePosition()
      if (objectiveFunction(currPos) > objectiveFunction(localBest)) {
        println("New local best found!")
        localBest = currPos
      }
      if (objectiveFunction(currPos) > objectiveFunction(globalBest)) {
        println("New global best found!")
        globalBest = currPos
        neighbourhood.foreach(n => n ! UpdateGlobalBest(globalBest))
      }
    case UpdateGlobalBest(best: List[Double]) =>
      println(s"Updating my global best value to: $best")
      globalBest = best
    case UpdateNeighbourhood(neighbours: Set[ActorRef]) => 
      println("Updating neighbours")
      neighbourhood = neighbours
    case _ => 
      println("do shit when they talk")
  }

  def UpdateVelocity(): List[Double] = {
    currVel.zip(currPos).zip(localBest.zip(globalBest)).map { 
      case ((v, p), (l, g)) =>
        (w * v) + (c1 * r1 * (l - p)) + (c2 * r2 * (g - p))
      case _ => throw new RuntimeException()
    }
  }

  def UpdatePosition(): List[Double] = {
    currVel.zip(currPos).map {
      case (v, p) => p + v
      case _ => throw new RuntimeException()
    }
  }

}

