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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PSOExecutor(pso: PSOSystem) extends Executor {
  override def run(): Future[Result] = {
    val particleCount = pso.particles.length

    println(s"\nExecuting job with:")
    println(s"\tParticle count: $particleCount")
    println(s"\tOptimization Function: ${pso.config.objectiveFunction}")
    println(s"\tTermination Criteria: ${pso.config.terminationCriteria}")

    pso.master ! PrintSwarmScore

    // termination criteria is # of iterations
    for(i <- 1 to 1000) {
      pso.particles.foreach(p => p ! ComputeIteration)
    }

    pso.system.shutdown()
    Future{Result(finalValue = 1)}
  }
}
