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
package pso

import akka.actor.ActorRef

/** Determines how the particles are connected to each other.
 *  For visualization of the topologies see Figure 1 in the document below:
 *  https://www.dropbox.com/s/u6ndn09xd87m1zt/PSO%20Topologies.pdf?dl=0
 */
object ParticleTopology extends Enumeration {
  type ParticleTopology = Value
  val Ring, FullyConnected, Grid, Toroidal, Star, BinaryTree = Value

  private def connectParticlePair(a: ActorRef, b: ActorRef) = {
    a ! NewNeighbour(b)
    b ! NewNeighbour(a)
  }

  def formRing(particles: List[ActorRef]): Unit = {
    connectParticlePair(particles.head, particles.takeRight(1).head)
    formChain(particles)
  }

  private def formChain(particles: List[ActorRef]): Unit = {
    if (particles.size > 1) {
      connectParticlePair(particles.head, particles.tail.head)
      formChain(particles.tail)
    }
  }

  def fullyConnected(particles: Set[ActorRef]): Unit = {
    particles.foreach(p => p ! UpdateNeighbourhood(particles.diff(Set(p))))
  }

  private def buildGrid(particles: List[ActorRef]):
    (Int, List[(Int, Int)], Array[Array[ActorRef]]) = {
    val particleCount = particles.size
    val w = math.sqrt(particleCount)
    val width = if (w.isValidInt) {
      w.toInt
    } else {
      throw new RuntimeException(
        "To use this topology particles must fit in a square grid")
    }
    val cols = (1 to particleCount).map(p => (p - 1) % width)
    val rows = (1 to particleCount).map(p => (p - 1) / width)
    val pos = rows.zip(cols)
    val particlePos = particles.zip(pos)
    val particleGrid = new Array[Array[ActorRef]](width).map(_ =>
      new Array[ActorRef](width))
    particlePos.foreach(p => particleGrid(p._2._1)(p._2._2) = p._1)
    (width, pos.toList, particleGrid)
  }

  private def connectGrid(width: Int, positions: List[(Int,Int)],
    grid: Array[Array[ActorRef]]) = {
    positions.foreach{case (row, col) =>
      row match {
        case r if r <= 0 =>
          connectParticlePair(grid(row)(col), grid(row + 1)(col))
        case r if r > 0 && r < width - 1 =>
          connectParticlePair(grid(row)(col), grid(row + 1)(col))
          connectParticlePair(grid(row)(col), grid(row - 1)(col))
        case r if r >= width - 1 =>
          connectParticlePair(grid(row)(col), grid(row - 1)(col))
      }
      col match {
        case c if c <= 0 =>
          connectParticlePair(grid(row)(col), grid(row)(col + 1))
        case c if c > 0 && c < width - 1 =>
          connectParticlePair(grid(row)(col), grid(row)(col + 1))
          connectParticlePair(grid(row)(col), grid(row)(col - 1))
        case c if c >= width - 1 =>
          connectParticlePair(grid(row)(col), grid(row)(col - 1))
      }
    }
  }

  def formGrid(particles: List[ActorRef]): Unit = {
    val (width, pos, grid) = buildGrid(particles)
    connectGrid(width, pos, grid)
  }

  private def connectToroidal(width: Int, grid: Array[Array[ActorRef]]) = {
    (0 until width).foreach{col =>
      val firstRow = 0
      val lastRow = width - 1
      connectParticlePair(grid(firstRow)(col), grid(lastRow)(col))
    }
    (0 until width).foreach{row =>
      val firstCol = 0
      val lastCol = width - 1
      connectParticlePair(grid(row)(firstCol), grid(row)(lastCol))
    }
  }

  def formToroidal(particles: List[ActorRef]): Unit = {
    val (width, pos, grid) = buildGrid(particles)
    connectGrid(width, pos, grid)
    connectToroidal(width, grid)
  }

  def formStar(particles: List[ActorRef]): Unit = {
    particles.tail.foreach(p => connectParticlePair(p, particles.head))
  }

  def formBinaryTree(particles: List[ActorRef]): Unit = {
    val particleCount = particles.length
    (1 to particleCount).map(p => (p, (p * 2, p * 2 + 1)))
      .foreach{ case (p, (left, right)) =>
        if (left < particleCount) {
          connectParticlePair(particles(p - 1), particles(left - 1))
        }
        if (right < particleCount) {
          connectParticlePair(particles(p - 1), particles(right - 1))
        }
      }
  }
}
