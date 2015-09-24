![Myriad](https://cloud.githubusercontent.com/assets/4529818/9751260/a6dddf84-5670-11e5-9bf3-a8d7fbf722b9.jpg)
Myriad
======

A Scala Akka library for swarm intelligence algorithms

[ ![Codeship Status for trifectalabs/myriad](https://codeship.com/projects/65153110-4494-0133-5e73-4a5ed300113a/status?branch=master)](https://codeship.com/projects/104435)

###How It Works
--------------
Integration with Myriad involves defining the problem you're aiming to solve. This is done by fufilling either the ACO or PSO contract, depending on which algorithm is more appropriate to your use case. 

######1. Create a class that extends the respective interface

  ```
  class MyPSOClass extends PSOInterface {
    ...
  }
  ```
  or

  ```
  class MyACOClass extends ACOInterface {
    ...
  }
  ```

######2. Override the objective function, defining it to fit your requirements.

  ```
  class MyClass extends PSOInterface {
    override def objectiveFunction(particle: Solution): Double = {
      if particle.value > 5
        particle.value 
      else 
        5
    }
    ...
  }
  ```

######3. Override any optional functions or values to gain more control of the operation of the swarm.

  ```
  class MyClass extends PSOInterface {
    override def objectiveFunction(particle: Solution): Double = {
      if particle.value > 5
        particle.value 
      else 
        5
    }
    
    override def terminationCriteria: Boolean = {
      false
    }
  }
  ```
