trait SwarmInterface {
  // The objective function to maximize 
  def objectiveFunction(solution: Worker): Double 

  // Optional things to specify, like termination criteria
  def terminationCriteria: Boolean  
}
