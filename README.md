![Myriad](https://cloud.githubusercontent.com/assets/4529818/9751260/a6dddf84-5670-11e5-9bf3-a8d7fbf722b9.jpg)
Myriad
======

A Scala Akka library for swarm intelligence algorithms

[ ![Codeship Status for trifectalabs/myriad](https://codeship.com/projects/65153110-4494-0133-5e73-4a5ed300113a/status?branch=master)](https://codeship.com/projects/104435)

###How It Works
--------------

######1. Create an objective function which looks something like this

  ```
  def objectiveFunction(X: List[Double]): Double {
    ...
  }
  ```

######2. Declare a config using your objective function and specify some initial solutions
The number of solutions specified will determine the size of the swarm. The only two required parameters are the objective function and initial solutions but all of the other parameters are customizable as well.

  ```
  val conf = PSOConfiguration(
    objectiveFunction = obj,
    initialSolutions = solutions,
    ...
  )
  ```

######3. Create a factory to build the optimization system

  ```
  val psoSystemFactory = new PSOSystemFactory(conf)
  val pso = psoSystemFactory.build()
  ```

######4. Create an exectuor to run the optimization

  ```
  val psoJob = new PSOExecutor(pso)
  psoJob.run
  ```
