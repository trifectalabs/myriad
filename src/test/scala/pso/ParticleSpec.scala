// Copyright (C) 2015 Josiah Witt and Christopher Poenaru
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
// 02110-1301, USA.

package pso

import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad.{ParticleState, SwarmMaster, ProblemType}
import com.trifectalabs.myriad.pso._
import example.SampleFunctions

import akka.actor.{Props, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import scala.concurrent.duration._
import akka.testkit.TestActorRef

class ParticleSpec
  extends TestKit(ActorSystem("ParticleSpec"))
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
  val particles = pso.particles

  val masterRef: TestActorRef[SwarmMaster] = TestActorRef(
    Props(new SwarmMaster(
      objectiveFunction =
        X => SampleFunctions.sphere(X.asInstanceOf[List[Double]]),
      problemType = ProblemType.Minimization,
      terminationCriteria = TerminationCriteria())))
  val master = masterRef.underlyingActor
  val particleRef: TestActorRef[Particle] = TestActorRef(Props(new Particle(
    initVelocity = List(1.0, 1.0),
    inertia = 0.792,
    localAccel = 1.4944,
    neighbourhoodAccel = 1.4944,
    lbest = List(-1.5, -0.5),
    nbest = List(-0.1, 0.1),
    problemType = ProblemType.Minimization,
    objectiveFunction = SampleFunctions.sphere,
    particleSearchSpace = None,
    master = masterRef,
    id = 0,
    randomSeed = Some(0))))
  val particle = particleRef.underlyingActor

  override def afterAll() {
    shutdown()
  }

  "A single particle" should {
    "calculate a new velocity" in {
      particle.calculateNewVelocity()
      // newVel = (inertia * oldVel) +
      //  (localAccel * firstRandomFromSeed * (lbest - position)) +
      //  (neighbourhoodAccel * nextRandomFromSeed * (nbest - position))
      //
      // 1.2952406674112558 = (0.792 * 1.0) +
      //  (1.4944 * 0.730967787376657 * (-1.5 - -1.5)) +
      //  (1.4944 * 0.24053641567148587 * (-0.1 - -1.5))
      //
      // 1.2855438362686753 = (0.792 * 1.0) +
      //  (1.4944 * 0.6374174253501083 * (-0.5 - -0.5)) +
      //  (1.4944 * 0.5504370051176339 * (0.1 - -0.5))
      assert(particle.currVel == List(1.2952406674112558, 1.2855438362686753))
    }

    "calculate a new position" in {
      particle.calculateNewPosition()
      // newPos = oldPos + newVel
      //
      // -0.20475933258874424 = -1.5 + 1.2952406674112558
      //
      // 0.7855438362686753 = -0.5 + 1.2855438362686753
      assert(particle.currPos == List(-0.20475933258874424, 0.7855438362686753))
    }

    "update local best if better solution is found" in {
      particle.updateLocalBestIfNecessary()
      // local best is updated to current position since it is closer
      // to optimal than the previous local best
      //
      // sphere(List(-1.5, -0.5)) = 2.5
      //
      // sphere(List(-0.20475933258874424, 0.7855438362686753)) =
      //  0.6590055029818953
      assert(
        particle.localBest == List(-0.20475933258874424, 0.7855438362686753))
    }

    "not update neighbourhood best if better solution is not found" in {
      particle.updateNeighbourhoodBestIfNecessary()
      // neighbourhood best is not updated to current position since it
      // is further from optimal than the current neighbourhood best
      //
      // sphere(List(-0.1, 0.1)) = 0.02
      //
      // sphere(List(-0.20475933258874424, 0.7855438362686753)) =
      //  0.6590055029818953
      assert(particle.neighbourhoodBest == List(-0.1, 0.1))
    }

    "not update if a worse solution is received" in {
      particle.receiveNeighbourhoodUpdate(List(-1.5, -0.5))
      // when a neighbourhood best update is received for an neighbour
      // the particle's known neighbourhood best should only update if
      // the received position is better
      //
      // sphere(List(-1.5, -0.5)) = 2.5
      //
      // sphere(List(-0.1, 0.1)) = 0.02
      assert(particle.neighbourhoodBest == List(-0.1, 0.1))
    }

    "update if better solution is received" in {
      particle.receiveNeighbourhoodUpdate(List(0.0, 0.0))
      // same as previous test but position should update
      //
      // sphere(List(0.0, 0.0)) = 0.0
      //
      // sphere(List(-0.1, 0.1)) = 0.02
      assert(particle.neighbourhoodBest == List(0.0, 0.0))
    }

    "check in with a master" in {
      particle.workerCheckIn()
      // after a particle checks in with its master, the master should
      // have an up to date particle state
      assert(master.swarmState(particleRef) == ParticleState(
        velocity = List(1.2952406674112558, 1.2855438362686753),
        solution = List(-0.20475933258874424, 0.7855438362686753),
        lbest = List(-0.20475933258874424, 0.7855438362686753),
        best = List(0.0, 0.0),
        iteration = 0))
    }
  }

  "A particle in a swarm" should {
    "update its velocity, position, local best and neighbourhood best" in {
      within(500.millis) {
        particles.head ! ReportRequest
        expectMsg(
          Report(
            vel = List(5.0, 5.0),
            pos = List(-5.0, -5.0),
            lbest = List(-5.0, -5.0),
            nbest = List(-3.0, -3.0)))
        particles.head ! ComputeIteration
        particles.head ! ReportRequest
        expectMsg(
          Report(
            vel = List(4.678915239158937, 5.0),
            pos = List(-0.32108476084106297, 0.0),
            lbest = List(-0.32108476084106297, 0.0),
            nbest = List(-0.32108476084106297, 0.0)))
      }
    }

    "update its neighbour's neighbourhood best" in {
      within(500.millis) {
        particles(1) ! ReportRequest
        expectMsg(
          Report(
            vel = List(5.0, 5.0),
            pos = List(-4.0, -4.0),
            lbest = List(-4.0, -4.0),
            nbest = List(-0.32108476084106297, 0.0)))
        particles(2) ! ReportRequest
        expectMsg(
          Report(
            vel = List(5.0, 5.0),
            pos = List(-3.0, -3.0),
            lbest = List(-3.0, -3.0),
            nbest = List(-0.32108476084106297, 0.0)))
      }
    }
  }
}

