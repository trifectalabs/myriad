trait SwarmInterface {
  // The objective function to maximize 
  def objectiveFunction(particle: Solution): Double 

  // Optional things to specify, like termination criteria
  def terminationCriteria: Boolean  
}
