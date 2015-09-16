package com.trifectalabs.myriad
package aco

sealed trait Ant extends Worker {
  override def receive = {
    case _ => 
      println("do shit when they talk")
  }

  override val solution = List[Int]()
}

class AntImpl extends Ant {

}
