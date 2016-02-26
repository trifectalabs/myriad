// Copyright (C) 2015 Josiah Witt and Christopher Poenaru
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
// 02110-1301, USA.

package aco

import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.trifectalabs.myriad._
import com.trifectalabs.myriad.aco._

import akka.actor.{Props, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import scala.concurrent._
import scala.concurrent.duration._
import akka.testkit.TestActorRef

class AntSpec
  extends TestKit(ActorSystem("AntSpec"))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  // setup
  val t = Map(
    (0, 1, 0) -> 2,
    (0, 2, 0) -> 2,
    (0, 3, 0) -> 1,
    (0, 4, 0) -> 4,
    (1, 2, 0) -> 3,
    (1, 3, 0) -> 2,
    (1, 4, 0) -> 3,
    (2, 3, 0) -> 2,
    (2, 4, 0) -> 2,
    (3, 4, 0) -> 4,
    (1, 0, 0) -> 2,
    (2, 0, 0) -> 2,
    (3, 0, 0) -> 1,
    (4, 0, 0) -> 4,
    (2, 1, 0) -> 3,
    (3, 1, 0) -> 2,
    (4, 1, 0) -> 3,
    (3, 2, 0) -> 2,
    (4, 2, 0) -> 2,
    (4, 3, 0) -> 4)

  override def afterAll() {
    shutdown()
  }

  def tsp(x: Int, y: Int, z: Int, p: List[Path]): Double = {
    t((x,y,z)).toDouble
  }

  "Ant colony" should {
    "minimize distance of travelling salesman" in {
      val conf = ACOConfiguration(
        tsp,
        5,
        5,
        List((0,1),(0,2),(0,3),(0,4),(1,2),(1,3),(1,4),(2,3),(2,4),(3,4)))
      val acoSystemFactory = new ACOSystemFactory(conf)
      val aco = acoSystemFactory.build()
      val acoJob = new ACOExecutor(aco)
      Await.result(acoJob.run, 5 seconds) match {
        case Result(Right(x)) =>
          assert(x.map(_.distance.get).reduce(_ + _) == 10.0)
        case _ =>
          assert(1 == 0)
      }
    }
  }
}
