// Copyright (C) 2015 Josiah Witt and Christopher Poenaru
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
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

package com.trifectalabs.myriad
package pso

import akka.actor.{ActorSystem, Props}

class PSOSystemFactory(config: PSOConfiguration) {
  val problemType = config.problemType
  val solutions = config.initialSolutions
  val topology = config.topology
  private def obj = config.objectiveFunction

  def build(randomSeed: Option[Int] = None): PSOSystem = {
    val system = ActorSystem("SwarmSystem")
    val r = randomSeed match {
      case None => new scala.util.Random(System.currentTimeMillis)
      case Some(seed) => new scala.util.Random(seed)
    }
    val bestSolution = solutions.sortWith{
      case (sol1, sol2) =>
        ProblemType.compare(problemType, obj(sol1), obj(sol2))
    }.head
    val velocity = config.initialVelocity match {
      case None => Seq.fill(solutions.head.length)(0.0)
      case Some(v) => v
    }
    // Create particles from initial solutions
    val particles = solutions.map{solution =>
      system.actorOf(Props(classOf[Particle],
        velocity, config.inertia, config.localAccel,
        config.neighbourhoodAccel, r.nextDouble(),
        r.nextDouble(), solution, bestSolution,
        config.problemType, obj)
      )
    }
    // connect particles based on topology
    topology match {
      case ParticleTopology.Ring =>
        ParticleTopology.formRing(particles)
      case ParticleTopology.FullyConnected =>
        ParticleTopology.fullyConnected(particles.to[Set])
      case ParticleTopology.Grid =>
        ParticleTopology.formGrid(particles)
      case ParticleTopology.Toroidal =>
        ParticleTopology.formToroidal(particles)
      case ParticleTopology.Star =>
        ParticleTopology.formStar(particles)
      case ParticleTopology.BinaryTree =>
        ParticleTopology.formBinaryTree(particles)
    }
    PSOSystem(system, config, particles)
  }
}
