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

import scala.util.Random

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad.ProblemType
import com.trifectalabs.myriad.pso.{PSOConfiguration, PSOSystemFactory,
  UpdateNeighbourhood, ParticleTopology, GetNeighbourhood, CurrentNeighbourhood}
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import scala.concurrent.duration._

class ParticleTopologySpec
  extends TestKit(ActorSystem("ParticleTopologySpec"))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  // setup
  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }
  val r = new Random(System.currentTimeMillis)
  val particleCount = 9
  val solutions = Seq.fill(particleCount)(List(
    (5.0-(-5.0))*r.nextDouble-5.0,
    (5.0-(-5.0))*r.nextDouble-5.0
  )).toList
  val conf = PSOConfiguration(
    problemType = ProblemType.Minimization,
    objectiveFunction = obj,
    initialSolutions = solutions
  )
  val psoSystemFactory = new PSOSystemFactory(conf)
  val pso = psoSystemFactory.build()
  val particles = pso.particles
  // clear neighbourhoods before testing
  particles.foreach(_ ! UpdateNeighbourhood(Set()))

  override def afterAll {
    shutdown()
  }

  "The particles" should {
    "be connected in a ring topology" in {
      within(500.millis) {
        ParticleTopology.formRing(particles)
        // first connected to last and second
        particles.head ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(Set(particles.takeRight(1).head, particles(1)))
        )
        // last connected to first and second last
        particles.takeRight(1).head ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(Set(particles.head, particles.takeRight(2).head))
        )
        (1 until particles.length - 1).foreach{ x =>
          particles(x) ! GetNeighbourhood
          expectMsg(
            CurrentNeighbourhood(Set(particles(x - 1), particles(x + 1)))
          )
        }
        // clear neighbourhoods for next test
        particles.foreach(_ ! UpdateNeighbourhood(Set()))
      }
    }

    "be connected in a fully connected topology" in {
      within(500.millis) {
        val particleSet = particles.to[Set]
        ParticleTopology.fullyConnected(particleSet)
        // every particle connected to every other particle
        particleSet.foreach{ p =>
          p ! GetNeighbourhood
          expectMsg(CurrentNeighbourhood(particleSet.diff(Set(p))))
          // clear neighbourhoods for next test
          p ! UpdateNeighbourhood(Set())
        }
      }
    }

    "be connected in a grid topology" in {
      within(500.millis) {
        ParticleTopology.formGrid(particles)
        // top left corner
        particles.head ! GetNeighbourhood
        expectMsg(CurrentNeighbourhood(Set(particles(1), particles(3))))
        // top side
        particles(1) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(Set(particles.head, particles(2), particles(4)))
        )
        // top right corner
        particles(2) ! GetNeighbourhood
        expectMsg(CurrentNeighbourhood(Set(particles(1), particles(5))))
        // left side
        particles(3) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(Set(particles.head, particles(4), particles(6)))
        )
        // center
        particles(4) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(1), particles(3), particles(5), particles(7))
          )
        )
        // right side
        particles(5) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(Set(particles(2), particles(4), particles(8)))
        )
        // bottom left corner
        particles(6) ! GetNeighbourhood
        expectMsg(CurrentNeighbourhood(Set(particles(3), particles(7))))
        // bottom side
        particles(7) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(Set(particles(4), particles(6), particles(8)))
        )
        // bottom right corner
        particles(8) ! GetNeighbourhood
        expectMsg(CurrentNeighbourhood(Set(particles(5), particles(7))))
        // clear neighbourhoods for next test
        particles.foreach(_ ! UpdateNeighbourhood(Set()))
      }
    }

    "be connected in a toroidal topology" in {
      within(500.millis) {
        ParticleTopology.formToroidal(particles)
        // top left corner
        particles.head ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(1), particles(3), particles(2), particles(6))
          )
        )
        // top side
        particles(1) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles.head, particles(2), particles(4), particles(7))
          )
        )
        // top right corner
        particles(2) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(1), particles(5), particles.head, particles(8))
          )
        )
        // left side
        particles(3) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles.head, particles(4), particles(6), particles(5))
          )
        )
        // center
        particles(4) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(1), particles(3), particles(5), particles(7))
          )
        )
        // right side
        particles(5) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(2), particles(4), particles(8), particles(3))
          )
        )
        // bottom left corner
        particles(6) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(3), particles(7), particles.head, particles(8))
          )
        )
        // bottom side
        particles(7) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(4), particles(6), particles(8), particles(1))
          )
        )
        // bottom right corner
        particles(8) ! GetNeighbourhood
        expectMsg(
          CurrentNeighbourhood(
            Set(particles(5), particles(7), particles(2), particles(6))
          )
        )
        // clear neighbourhoods for next test
        particles.foreach(_ ! UpdateNeighbourhood(Set()))
      }
    }

    "be connected in a star topology" in {
      within(500.millis) {
        ParticleTopology.formStar(particles)
        // center
        particles.head ! GetNeighbourhood
        expectMsg(CurrentNeighbourhood(particles.tail.to[Set]))
        // points
        particles.tail.foreach{ p =>
          p ! GetNeighbourhood
          expectMsg(CurrentNeighbourhood(Set(particles.head)))
        }
        // clear neighbourhoods for next test
        particles.foreach(_ ! UpdateNeighbourhood(Set()))
      }
    }

    "be connected in a binary tree topology" in {
      within(500.millis) {
        ParticleTopology.formBinaryTree(particles)
        (1 until particles.length).foreach{ x =>
          particles(x - 1) ! GetNeighbourhood
          val parent = if (x / 2 > 0) {
            Some(particles(x / 2 - 1))
          } else { None }
          val left = if (x * 2 < particles.length) {
            Some(particles(x * 2 - 1))
          } else { None }
          val right = if (x * 2 + 1 < particles.length) {
            Some(particles(x * 2))
          } else { None }
          val neighbourhood = Set(parent, left, right)
          expectMsg(CurrentNeighbourhood(neighbourhood.flatten))
        }
      }
    }
  }
}

