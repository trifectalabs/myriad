trait ACOInterface extends SwarmInterface {
  import PheromoneUpdate._

  override def objectiveFunction(ant: Worker): Double 

  // The default termination criteria can be specified here
  override def terminationCriteria: Boolean = {
    false
  }

  // The default pheromone update scheme can be set here 
  def pheromoneUpdateScheme: PheromoneUpdate = Density
}

