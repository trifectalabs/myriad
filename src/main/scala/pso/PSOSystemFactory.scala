// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package pso

import akka.actor.{ActorSystem, Props}

class PSOSystemFactory(config: PSOConfiguration) {
  private val problemType = config.problemType
  private val solutions = config.initialSolutions
  private val topology = config.topology
  private def obj = config.objectiveFunction

  def build(randomSeed: Option[Long] = None,
    actorSystem: Option[ActorSystem] = None): PSOSystem = {
    val system = actorSystem.getOrElse(ActorSystem("PSOSwarmSystem"))
    val master = system.actorOf(Props(new SwarmMaster(
      // SwarmMaster requires objective function taking a List[AnyVal] as
      // it is general to all optimization algorithms
      X => obj(X.asInstanceOf[List[Double]]),
      config.problemType,
      config.terminationCriteria)))
    val bestSolution = solutions.sortWith{
      case (sol1, sol2) =>
        ProblemType.compare(problemType, obj(sol1), obj(sol2))
    }.head
    val velocity = config.initialVelocity.getOrElse(
      Seq.fill(solutions.head.length)(0.0))
    // Create particles from initial solutions
    val particles = solutions.zipWithIndex.map{ case (solution, id) =>
      system.actorOf(Props(new Particle(
        velocity.toList, config.inertia, config.localAccel,
        config.neighbourhoodAccel, solution, bestSolution,
        config.problemType, obj, config.searchSpace, master,
        id, randomSeed)))
    }
    // Connect particles based on topology
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
    PSOSystem(system, config, master, particles)
  }
}
