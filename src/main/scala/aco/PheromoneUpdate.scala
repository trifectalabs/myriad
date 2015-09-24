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
package aco

sealed trait PheromoneUpdate

object PheromoneUpdate {
  // Adds Q, a constant value.
  case object Density extends PheromoneUpdate {
    override def toString: String = "Density"

    // This function calculates the value of Q
    def calculate: Double = {
      val Q = 10
      Q
    }
  }

  // Adds Q/distance, taking edge length into account.
  case object Quantity extends PheromoneUpdate {
    override def toString: String = "Quantity"

    // This function calculates the value of Q
    def calculate: Double = {
      val Q = 10
      Q
    }
  }

  // Updates pheromone after soln is found. ∆T = Q/L(t), L = length of path
  case object Delayed extends PheromoneUpdate {
    override def toString: String = "Delayed"

    // This function calculates the value of Q
    def calculate(length: Double): Double = {
      val Q = 10/length
      Q
    }
  }
}
