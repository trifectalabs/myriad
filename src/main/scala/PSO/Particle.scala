import Main._
import akka.actor._

sealed trait ParticleMessage
case object CalculateNewPosition extends ParticleMessage
case class UpdateNeighbourhoodBest(best: List[Double]) extends ParticleMessage
case class UpdateNeighbourhood(neighbours: Set[ActorRef]) extends ParticleMessage
case object Report extends ParticleMessage

class Particle(id: Int, initVelocity: List[Double], inertiaW: Double, accelCoeff1: Double, 
    accelCoeff2: Double, randCoeff1: Double, randCoeff2: Double, lbest: List[Double], 
    nbest: List[Double], objectiveFunction: List[Double] => Double) extends Actor {
  val w = inertiaW
  val c1 = accelCoeff1
  val c2 = accelCoeff2
  val r1 = randCoeff1
  val r2 = randCoeff2
  var currVel = initVelocity
  var currPos = lbest
  var localBest = lbest
  var neighbourhoodBest = nbest
  var neighbourhood = Set[ActorRef]()
  
  override def receive = {
    case CalculateNewPosition =>
      val oldPos = currPos
      val oldVel = currVel
      currVel = UpdateVelocity()
      currPos = UpdatePosition()
      println(s"Particle $id changed velocity from $oldVel to $currVel")
      println(s"Particle $id moved from $oldPos to $currPos")
      if (objectiveFunction(currPos) > objectiveFunction(localBest)) {
        val oldLBest = localBest
        localBest = currPos
        println(s"Particle $id improved local best from $oldLBest to $localBest")
      }
      if (objectiveFunction(currPos) > objectiveFunction(neighbourhoodBest)) {
        val oldNBest = neighbourhoodBest
        neighbourhoodBest = currPos
        println(s"Particle $id improved neighbourhood best from $oldNBest to $neighbourhoodBest")
        neighbourhood.foreach(n => n ! UpdateNeighbourhoodBest(neighbourhoodBest))
      }
    case UpdateNeighbourhoodBest(best: List[Double]) =>
      if (objectiveFunction(best) > objectiveFunction(neighbourhoodBest)) {
        println(s"Particle $id updating neighbourhood best value to $best")
        neighbourhoodBest = best
      } else {
        println(s"Particle $id not updating neighbourhood best value to $best")
      }
    case UpdateNeighbourhood(neighbours: Set[ActorRef]) => 
      println(s"Particle $id updating neighbours")
      neighbourhood = neighbours
    case Report => println(s"Particle $id: lbest = $localBest, nbest = $neighbourhoodBest")
    case _ => 
      println("do shit when they talk")
  }

  def UpdateVelocity(): List[Double] = {
    currVel.zip(currPos).zip(localBest.zip(neighbourhoodBest)).map { 
      case ((v, p), (l, n)) =>
        (w * v) + (c1 * r1 * (l - p)) + (c2 * r2 * (n - p))
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

