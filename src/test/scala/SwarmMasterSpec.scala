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

package com.trifecta.myriad

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad.{ ProblemType, SwarmRequest, SwarmReport,
  ParticleState }
import com.trifectalabs.myriad.pso.{ PSOConfiguration, PSOSystemFactory,
  ParticleTopology, ComputeIteration }
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import scala.concurrent.duration._

class SwarmMonitorSpec
  extends TestKit(ActorSystem("SwarmMonitorSpec"))
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
  val master = pso.master
  val particles = pso.particles

  override def afterAll {
    shutdown()
  }

  "A master" should {
    "know about all the particles" in {
      within(500.millis) {
        master ! SwarmRequest
        val expectedState = Map(
          particles.head -> ParticleState(
            List(5.0, 5.0),
            List(-5.0, -5.0),
            List(-5.0, -5.0),
            List(-3.0, -3.0)
          ),
          particles(1) -> ParticleState(
            List(5.0, 5.0),
            List(-4.0, -4.0),
            List(-4.0, -4.0),
            List(-3.0, -3.0)
          ),
          particles(2) -> ParticleState(
            List(5.0, 5.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0)
          )
        )
        expectMsg(SwarmReport(expectedState))
      }
    }

    "update the state of the swarm" in {
      within(500.millis) {
        particles.head ! ComputeIteration
        Thread.sleep(10)
        master ! SwarmRequest
        val expectedState = Map(
          particles.head -> ParticleState(
            List(4.678915239158937, 4.678915239158937),
            List(-0.32108476084106297, -0.32108476084106297),
            List(-0.32108476084106297, -0.32108476084106297),
            List(-0.32108476084106297, -0.32108476084106297)
          ),
          particles(1) -> ParticleState(
            List(5.0, 5.0),
            List(-4.0, -4.0),
            List(-4.0, -4.0),
            List(-3.0, -3.0)
          ),
          particles(2) -> ParticleState(
            List(5.0, 5.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0),
            List(-3.0, -3.0)
          )
        )
        expectMsg(SwarmReport(expectedState))
      }
    }
  }
}

