sealed trait PheromoneUpdate

object PheromoneUpdate {
  // Adds Q, a constant value.
  case object Density extends PheromoneUpdate {
    override def toString = "Density"

    //This function calculates the value of Q
    def calculate: Double = {
      10 
    }
  }

  // Adds Q/distance, taking edge length into account.
  case object Quantity extends PheromoneUpdate {
    override def toString = "Quantity"

    //This function calculates the value of Q
    def calculate: Double = {
      10 
    }
  }

  // Updates pheromone after soln is found. ∆T = Q/L(t), L = length of path
  case object Delayed extends PheromoneUpdate {
    override def toString = "Delayed"

    //This function calculates the value of Q
    def calculate(length: Double): Double = {
      10/length 
    }
  }
}


