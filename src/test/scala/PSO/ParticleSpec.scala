package PSO

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad.ProblemType
import com.trifectalabs.myriad.pso._
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import scala.concurrent.duration._

class ParticleSpec
  extends TestKit(ActorSystem("ParticleSpec",
    ConfigFactory.parseString(ParticleSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  //setup
  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }
  val solutions = List(
    List(-5.0,-5.0), List(-4.0, -4.0), List(-3.0, -3.0)
  )
  val conf = PSOConfiguration(
    problemType = ProblemType.Minimization,
    objectiveFunction = obj,
    initialSolutions = solutions,
    initialVelocity = Some(List(5.0, 5.0)),
    topology = ParticleTopology.FullyConnected
  )
  val psoSystemFactory = new PSOSystemFactory(conf)
  val pso = psoSystemFactory.build(randomSeed = Some(0))
  val particles = pso.particles

  override def afterAll {
    shutdown()
  }

  "A particle" should {
    "update its velocity, position, local best and neighbourhood best" in {
      within(500.millis) {
        particles.head ! ReportRequest
        expectMsg(
          Report(
            List(5.0, 5.0),
            List(-5.0, -5.0),
            List(-5.0, -5.0),
            List(-3.0, -3.0))
        )
        particles.head ! ComputeIteration
        particles.head ! ReportRequest
        expectMsg(
          Report(
            List(4.678915239158937, 4.678915239158937),
            List(-0.32108476084106297, -0.32108476084106297),
            List(-0.32108476084106297, -0.32108476084106297),
            List(-0.32108476084106297, -0.32108476084106297)
          )
        )
      }
    }

    "update its neighbour's neighbourhood best" in {
      within(500.millis) {
        particles(1) ! ReportRequest
        expectMsg(
          Report(
            List(5.0, 5.0),
            List(-4.0, -4.0),
            List(-4.0, -4.0),
            List(-0.32108476084106297, -0.32108476084106297)
          )
        )
        particles(2) ! ReportRequest
        expectMsg(
          Report(
            List(5.0, 5.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0),
            List(-0.32108476084106297, -0.32108476084106297)
          )
        )
      }
    }
  }
}

object ParticleSpec {

  val config = """
    akka {
      loglevel = "WARNING"
    }
               """

}
