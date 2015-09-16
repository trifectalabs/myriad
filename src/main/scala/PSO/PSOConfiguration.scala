package com.trifectalabs.myriad
package pso

case class PSOConfiguration(
  numParticles: Int = 4,
  objectiveFunction: List[Double] => Double,
  terminationCriteria: Boolean = false)
