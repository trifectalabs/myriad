// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifecta.myriad

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad.{ProblemType, SwarmRequest, SwarmReport,
  ParticleState, SwarmMaster}
import com.trifectalabs.myriad.pso.{PSOConfiguration, PSOSystemFactory,
  ParticleTopology, ComputeIteration, TerminationCriteria}
import example.SampleFunctions
import com.typesafe.config.ConfigFactory

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit, TestActorRef }
import scala.concurrent.duration._

class SwarmMasterSpec
  extends TestKit(ActorSystem("SwarmMonitorSpec"))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  // setup
  val solutions = List(
    List(-5.0,-5.0), List(-4.0, -4.0), List(-3.0, -3.0))
  val conf = PSOConfiguration(
    problemType = ProblemType.Minimization,
    objectiveFunction = SampleFunctions.sphere,
    initialSolutions = solutions,
    initialVelocity = Some(List(5.0, 5.0)),
    topology = ParticleTopology.FullyConnected,
    terminationCriteria = TerminationCriteria(maxIterations = 1))
  val psoSystemFactory = new PSOSystemFactory(conf)
  val pso = psoSystemFactory.build(randomSeed = Some(0))
  val master = pso.master
  val particles = pso.particles

  val masterRef: TestActorRef[SwarmMaster] = TestActorRef(
    Props(new SwarmMaster(
      objectiveFunction =
        X => SampleFunctions.sphere(X.asInstanceOf[List[Double]]),
      problemType = ProblemType.Minimization,
      terminationCriteria = TerminationCriteria()))
  )
  val masterDirect = masterRef.underlyingActor

  override def afterAll {
    shutdown()
  }

  "A master" should {
    "update the swarm state based on particle check-in" in {
      val particleState = ParticleState(
        velocity = List(1.0, 2.0),
        solution = List(3.0, 4.0),
        lbest = List(5.0, 6.0),
        best = List(7.0, 8.0),
        iteration = 0)
      masterDirect.updateWorkerState(particleState)
      assert(
        masterDirect.swarmState == Map(masterDirect.sender() -> particleState))
    }

    "know about all the particles" in {
      // particles register with their master on creation so the master
      // should be aware of all particles in the swarm
      within(500.millis) {
        master ! SwarmRequest
        val expectedState = Map(
          particles.head -> ParticleState(
            velocity = List(5.0, 5.0),
            solution = List(-5.0, -5.0),
            lbest = List(-5.0, -5.0),
            best = List(-3.0, -3.0),
            iteration = 0),
          particles(1) -> ParticleState(
            velocity = List(5.0, 5.0),
            solution = List(-4.0, -4.0),
            lbest = List(-4.0, -4.0),
            best = List(-3.0, -3.0),
            iteration = 0),
          particles(2) -> ParticleState(
            velocity = List(5.0, 5.0),
            solution = List(-3.0, -3.0),
            lbest = List(-3.0, -3.0),
            best = List(-3.0, -3.0),
            iteration = 0))
        expectMsg(SwarmReport(expectedState))
      }
    }

    "update the state of the swarm" in {
      within(500.millis) {
        particles.head ! ComputeIteration
        Thread.sleep(50)
        master ! SwarmRequest
        val expectedState = Map(
          particles.head -> ParticleState(
            List(4.678915239158937, 5.0),
            List(-0.32108476084106297, 0.0),
            List(-0.32108476084106297, 0.0),
            List(-0.32108476084106297, 0.0),
            1),
          particles(1) -> ParticleState(
            List(5.0, 5.0),
            List(-4.0, -4.0),
            List(-4.0, -4.0),
            List(-3.0, -3.0),
            0),
          particles(2) -> ParticleState(
            List(5.0, 5.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0),
            0))
        expectMsg(SwarmReport(expectedState))
      }
    }
  }
}

