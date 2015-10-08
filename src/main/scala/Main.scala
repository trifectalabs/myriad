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

package com.trifectalabs.myriad

import pso.{PSOConfiguration, PSOSystemFactory, PSOExecutor,
  ParticleTopology, TerminationCriteria}
import example.SampleFunctions._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  def iter(particles: List[List[Double]], velocities: List[List[Double]],
    localBest: List[List[Double]], globalBest: List[Double],
    count: Int, obj: List[Double] => Double): List[Double] = {
    val (newParticles, newVelocities) =
      particles.zip(velocities).zip(localBest).map{
        case ((particle, velocity), lbest) =>
          particle.zip(velocity).zip(lbest.zip(globalBest)).map{
            case ((p, v), (lb, gb)) =>
              val r1 = r.nextDouble()
              val r2 = r.nextDouble()
              val newVel = scala.math.min(
                w * v + c1 * r1 * (lb - p) + c2 * r2 * (gb - p), 5.0)
              val newPos = p + newVel
              (newPos, newVel)
          }.unzip
      }.unzip
    val newLocalBest = newParticles.zip(localBest).map{ case (p, l) =>
      if (obj(p) < obj(l)) p else l
    }
    val newGlobalBest = newLocalBest.sortWith{ case (p1, p2) =>
      obj(p1) < obj(p2)
    }.head
    // particles.zip(velocities).zip(localBest).zipWithIndex.foreach{
    //   case (((particle, velocity), localBest), id) =>
    //     println(s"\n\nParticle $id Iteration $count\n" +
    //       s"Velocity: $velocity\nPosition: $particle\n" +
    //       s"Lbest: $localBest\nGbest: $newGlobalBest")
    // }
    if (count < maxIter) {
      iter(newParticles, newVelocities, newLocalBest, newGlobalBest,
        count + 1, obj)
    } else {
      newGlobalBest
    }
  }

  def psoSync(obj: List[Double] => Double): Unit = {
    val gbest = particles.sortWith{ case (p1, p2) =>
      obj(p1) < obj(p2)
    }.head
    val best = iter(particles, velocities, particles, gbest, 0, obj)
    val value = obj(best)
    println(s"\nBest of function: $best at $value")
  }

  def singleRun(obj: List[Double] => Double): Future[Result] = {
    val ss = Seq.fill(dimensions)((-10.0, 10.0)).toList
    val conf = PSOConfiguration(
      problemType = ProblemType.Minimization,
      objectiveFunction = obj,
      initialSolutions = particles,
      initialVelocity = Some(velocities.head),
      topology = ParticleTopology.FullyConnected,
      searchSpace = Some(ss),
      terminationCriteria = TerminationCriteria(maxIterations = maxIter)
    )
    val psoSystemFactory = new PSOSystemFactory(conf)
    val pso = psoSystemFactory.build()
    val psoJob = new PSOExecutor(pso)
    psoJob.run
  }

  def psoAsync(obj: List[Double] => Double): Unit = {
    singleRun(obj) onSuccess { case best =>
      best.finalValue match {
        case Left(v) =>
          val value = sphere(v)
          println(s"\nBest of function: $best at $value")
        case Right(v) =>
      }
    }
  }

  val r = new scala.util.Random(System.currentTimeMillis)
  val w = 0.792
  val c1 = 1.4944
  val c2 = 1.4944
  val maxIter = 1000
  val dimensions = 10
  val particleCount = 10
  val particles = Seq.fill(particleCount)(
    Seq.fill(dimensions)(10*r.nextDouble()).toList).toList
  val velocities = Seq.fill(particleCount)(
    Seq.fill(dimensions)(0.0).toList).toList
  // println("Synchronous Sphere")
  // psoSync(sphere)
  // println("\nAsynchronous Sphere")
  // psoAsync(sphere)
  val results = (1 to 30).map(_ => singleRun(sphere))
  Future.sequence(results) onSuccess { case r =>
    val all =
      r.map(_.finalValue).flatMap{value =>
        value match {
          case Left(v) =>
            Some(sphere(v))
          case Right(v) =>
            None
        }
      }
    val avg = all.sortWith(_ > _).drop(5).dropRight(5).sum / (r.length - 10)
    println(avg)
  }
}

