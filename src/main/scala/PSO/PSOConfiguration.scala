package com.trifectalabs.myriad
package pso

object ParticleTopology extends Enumeration {
  type ParticleTopology = Value
  val Ring, FullyConnected, Grid, Toroidal, Star = Value
}

object ProblemType extends Enumeration {
  type ProblemType = Value
  val Minimization, Maximization = Value
}

case class TerminationCriteria(
  maxIterations: Int = 1000,
  acceptableSolution: Option[Double] = None,
  improvementDelta: Option[Double] = None,
  improvementIter: Option[Int] = None
)

case class PSOConfiguration(
  objectiveFunction: List[Double] => Double,
  //solutionGenerator: Unit => List[Double],
  problemType: ProblemType.Value = ProblemType.Maximization,
  numParticles: Int = 10,
  topology: ParticleTopology.Value = ParticleTopology.FullyConnected,
  terminationCriteria: TerminationCriteria = TerminationCriteria(),
  inertia: Double = 0.792,
  localAccel: Double = 1.4944,
  neighbourhoodAccel: Double = 1.4944
)
