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

// Determines whether the objective function is maximized
// or minimized.
object ProblemType extends Enumeration {
  type ProblemType = Value
  val Minimization, Maximization = Value

  // Abtracted objective function comparison based on problem type
  def compare(problemType: ProblemType.Value,
    challenger: Double, best: Double): Boolean = {
    if (problemType == ProblemType.Minimization) challenger < best
    else challenger > best
  }
}
