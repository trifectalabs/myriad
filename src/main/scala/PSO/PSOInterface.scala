trait PSOInterface extends SwarmInterface {
  def objectiveFunction: Double 

  // The default termination criteria can be specified here
  override def terminationCriteria: Boolean = {
    false 
  }
}
