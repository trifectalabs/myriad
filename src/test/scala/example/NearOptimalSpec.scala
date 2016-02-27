// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.trifectalabs.myriad.{Result, ProblemType}
import com.trifectalabs.myriad.pso._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import SampleFunctions.{sphere, rastrigin, rosenbrock, ackley}

class NearOptimalSpec
  extends TestKit(ActorSystem("NearOptimalSpec"))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  // setup
  val r = new scala.util.Random(System.currentTimeMillis)
  val dimensions = 10
  val particleCount = 10
  val solutions = Seq.fill(particleCount)(
    Seq.fill(dimensions)(10*r.nextDouble()).toList).toList
  val ss = Seq.fill(dimensions)((-10.0, 10.0)).toList
  var pso: Option[PSOSystem] = None

  def singleRun(obj: List[Double] => Double): Future[Result] = {
    val conf = PSOConfiguration(
      problemType = ProblemType.Minimization,
      objectiveFunction = obj,
      initialSolutions = solutions,
      topology = ParticleTopology.FullyConnected,
      searchSpace = Some(ss),
      terminationCriteria = TerminationCriteria())
    val psoSystemFactory = new PSOSystemFactory(conf)
    pso = Some(psoSystemFactory.build(actorSystem = Some(system)))
    val psoJob = new PSOExecutor(pso.get)
    psoJob.run
  }

  // Run the optimization 30 times, drop the 5 worst and the 5 best and average
  // the 20 middle runs and assert that the average value is "near-optimal".
  //
  // "near-optimal" values are based on values from University of Waterloo
  // ECE 457A lecture slides with a margin of error added on.
  "A PSO job" should {
    "calculate near-optimal values for the sphere function" in {
      within(10.seconds) {
        val results = (1 to 30).map(_ => singleRun(sphere))
        Future.sequence(results) onSuccess { case r =>
          val avg =
            r.map(_.finalValue).flatMap{value =>
              value match {
                case Left(v) =>
                  Some(sphere(v))
                case Right(v) =>
                  None
              }
            }.sortWith(_ > _).drop(5).dropRight(5).sum / (r.length - 10)
          assert(avg < 2e-15)
          pso.get.particles.foreach(p => system.stop(p))
          system.stop(pso.get.master)
        }
      }
    }

    "calculate near-optimal values for the rosenbrock function" in {
      within(10.seconds) {
        val results = (1 to 30).map(_ => singleRun(rosenbrock))
        Future.sequence(results) onSuccess { case r =>
          val avg =
            r.map(_.finalValue).flatMap{value =>
              value match {
                case Left(v) =>
                  Some(rosenbrock(v))
                case Right(v) =>
                  None
              }
            }.sortWith(_ > _).drop(5).dropRight(5).sum / (r.length - 10)
          assert(avg < 15)
          pso.get.particles.foreach(p => system.stop(p))
          system.stop(pso.get.master)
        }
      }
    }

    "calculate near-optimal values for the rastrigin function" in {
      within(10.seconds) {
        val results = (1 to 30).map(_ => singleRun(rastrigin))
        Future.sequence(results) onSuccess { case r =>
          val avg =
            r.map(_.finalValue).flatMap{value =>
              value match {
                case Left(v) =>
                  Some(rastrigin(v))
                case Right(v) =>
                  None
              }
            }.sortWith(_ > _).drop(5).dropRight(5).sum / (r.length - 10)
          assert(avg < 20)
          pso.get.particles.foreach(p => system.stop(p))
          system.stop(pso.get.master)
        }
      }
    }

    "calculate near-optimal values for the ackley function" in {
      within(10.seconds) {
        val results = (1 to 30).map(_ => singleRun(ackley))
        Future.sequence(results) onSuccess { case r =>
          val avg =
            r.map(_.finalValue).flatMap{value =>
              value match {
                case Left(v) =>
                  Some(ackley(v))
                case Right(v) =>
                  None
              }
            }.sortWith(_ > _).drop(5).dropRight(5).sum / (r.length - 10)
          assert(avg < 1)
          pso.get.particles.foreach(p => system.stop(p))
          system.stop(pso.get.master)
        }
      }
    }
  }


}
