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

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad.ProblemType
import com.trifectalabs.myriad.pso.{ PSOConfiguration, PSOSystemFactory,
  ParticleTopology, ReportRequest, Report, ComputeIteration }
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import scala.concurrent.duration._

class ParticleSpec
  extends TestKit(ActorSystem("ParticleSpec",
    ConfigFactory.parseString(ParticleSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  // setup
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
    }"""

}
