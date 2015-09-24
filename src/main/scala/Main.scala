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

import pso.{PSOConfiguration, PSOSystemFactory, PSOExecutor}

object Main extends App {
  def obj(sol: List[Double]): Double = {
    sol.map(x => x*x).sum
  }
  val r = new scala.util.Random(System.currentTimeMillis)
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
  val psoJob = new PSOExecutor(pso)
  psoJob.run
}

