// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad
package aco

import akka.actor.{ActorRef, Actor}
import akka.event.Logging;

sealed trait AgentMessage
case object ResultRequest extends AgentMessage
case class BestRequest(returnRef: ActorRef) extends AgentMessage
case class ColonyBest(best: Option[List[Path]]) extends AgentMessage
case class StartNode(a: Node) extends AgentMessage
case class FinishNode(a: Node) extends AgentMessage
case class NewNeighbour(
  neighbourID: Int,
  neighbour: ActorRef) extends AgentMessage
case class AntMessage(
  antID: Int,
  path: List[Path],
  bestPath: Option[List[Path]],
  bestCost: Option[Double]) extends AgentMessage

class PlaceAgent(
  nodeNumber: Int,
  conf: ACOConfiguration,
  randomSeed: Option[Long]
) extends Actor {
  val distance = conf.distanceFunction
  val paths = conf.paths
  val alpha = conf.alpha
  val beta = conf.beta
  val Q = conf.Q
  val phi = 1.0 - conf.pheromoneDecayRate
  val node = Node(nodeNumber, self)
  var neighbourhood = scala.collection.mutable.HashMap.empty[Int, List[Path]]
  var antsSeen = scala.collection.mutable.HashMap.empty[Int, Int]
  var bestPathSeen: Option[List[Path]] = None
  var bestPathSeenCost: Option[Double] = None
  var start: Option[Node] = None
  var finish: Option[Node] = None

  val log = Logging(context.system, this)

  override def receive: Receive = {
    case ResultRequest =>
      self ! BestRequest(sender)
    case BestRequest(returnRef: ActorRef) =>
      val completed = (0 until conf.numberOfAnts)
        .map(a => antsSeen(a) >= conf.terminationCriteria.maxIterations)
        .reduce(_ && _)
      if (completed) returnRef ! ColonyBest(bestPathSeen)
      else self ! BestRequest(returnRef)
    case StartNode(a: Node) =>
      start = Some(a)
    case FinishNode(a: Node) =>
      finish = Some(a)
    case NewNeighbour(neighbourID: Int, neighbour: ActorRef) =>
      newNeighbour(neighbourID, neighbour)
    case AntMessage(
      antID: Int,
      path: List[Path],
      bestPath: Option[List[Path]],
      bestCost: Option[Double]) =>
      processAnt(antID, path, bestPath, bestCost)
  }

  def newNeighbour(id: Int, neighbour: ActorRef): Unit = {
    val nNode = Node(id, neighbour)
    if (neighbourhood contains id) {
      val index = neighbourhood(id).length + 1
      neighbourhood(id) = neighbourhood(id) :+ Path(node, nNode, index, 1.0)
    } else {
      neighbourhood += (id -> List(Path(node, nNode, 0, 0.5)))
    }
  }

  def processAnt(
    antID: Int,
    path: List[Path],
    bestPath: Option[List[Path]],
    bestCost: Option[Double]
  ): Unit = {
    // update iteration count
    if (!path.map(_.begin.id).contains(nodeNumber)) {
      if (antsSeen contains antID) antsSeen(antID) = antsSeen(antID) + 1
      else antsSeen(antID) = 1
    }
    updateIncomingPathPheromones(path)
    if (nodeNumber == finish.get.id && path.length > 0) {
      if (antsSeen(antID) < conf.terminationCriteria.maxIterations) {
        val cost = path.map(_.distance.get).reduce(_ + _)
        if (bestCost.isEmpty || cost < bestCost.get) {
          start.get.ref ! AntMessage(antID, List(), Some(path), Some(cost))
        } else {
          start.get.ref ! AntMessage(antID, List(), bestPath, bestCost)
        }
      }
    } else {
      val validPaths = getValidPaths(path)
      if (validPaths.length == 0 && start.get.id != finish.get.id) {
        // If no paths remaining, not at end node and not doing full tour
        // then something went wrong, log it and start again
        log.error("Lost ant, something went wrong")
        start.get.ref ! AntMessage(antID, List(), bestPath, bestCost)
      } else {
        val (nextPath, pathDist) = if (validPaths.length == 0) {
          pathToStart(path)
        } else {
          getNextPath(path, validPaths)
        }
        sendAntDownNextPath(antID, path, nextPath, pathDist, bestPath, bestCost)
        updatePathPheromones(nextPath, pathDist)
      }
    }
    // Update bestPathSeen if ant has better path
    if (!(bestCost.isEmpty && bestPathSeen.isEmpty) &&
      (bestPathSeenCost.isEmpty || bestCost.get < bestPathSeenCost.get)) {
      bestPathSeen = bestPath
    }
  }

  def getValidPaths(currentPath: List[Path]): List[Path] = {
    neighbourhood.values.flatten.filter(n =>
      if (conf.multiPathCollapse) {
        !currentPath.map(_.index).contains(n.index)
      } else {
        !currentPath.map(p => List(p.end.id, p.begin.id))
          .flatten
          .contains(n.end.id)
      }).toList
  }

  def pathToStart(currentPath: List[Path]): (Path, Double) = {
    val pathsToStart = if (conf.multiPathCollapse) {
      neighbourhood(start.get.id).filter(n =>
        !currentPath.map(_.index).contains(n.index))
    } else {
      neighbourhood(start.get.id)
    }
    val dist = pathsToStart.map(p =>
      (p, distance(nodeNumber, p.end.id, p.index, currentPath)))
    dist.sortWith(_._2 < _._2).head
  }

  def getNextPath(
    currentPath: List[Path],
    validPaths: List[Path]
  ): (Path, Double) = {
    // Calculate distances along each of the available paths
    val dist = validPaths.map(p =>
      (distance(nodeNumber, p.end.id, p.index, currentPath), p.pheromone))
    val sum = dist.map(s => math.pow(s._1, alpha) / math.pow(s._2, beta))
      .reduce(_ + _)
    val prob =
      dist.map(p => math.pow(p._1, alpha) / math.pow(p._2, beta) / sum)
    val r = new scala.util.Random(
      randomSeed.getOrElse(System.currentTimeMillis)).nextDouble()
    val nextPathIndex = prob.foldLeft((0.0, -1))((a, b) =>
      if (a._1 < r) (a._1 + b, a._2 + 1) else a)._2
    (validPaths(nextPathIndex), dist(nextPathIndex)._1)
  }

  def sendAntDownNextPath(
    antID: Int,
    currentPath: List[Path],
    nextPath: Path,
    pathDist: Double,
    bestPath: Option[List[Path]],
    bestCost: Option[Double]
  ): Unit = {
    nextPath.end.ref ! AntMessage(
      antID,
      currentPath :+ Path(node,
        nextPath.end,
        nextPath.index,
        nextPath.pheromone,
        Some(pathDist)),
      bestPath,
      bestCost)
  }

  def updateIncomingPathPheromones(currentPath: List[Path]): Unit = {
    // If bidirectional paths, update pheromone on path arrived from
    if (!conf.directedPaths && !currentPath.isEmpty) {
      neighbourhood(currentPath.last.begin.id) =
        neighbourhood(currentPath.last.begin.id).map(n =>
          if (n.index == currentPath.last.index) {
            n.copy(pheromone =
              phi*n.pheromone + Q/currentPath.last.distance.get)
          } else {
            n
          })
    }
  }

  def updatePathPheromones(nextPath: Path, pathDist: Double): Unit = {
    neighbourhood = neighbourhood.map{ case (id, paths) =>
      (id, paths.map(p =>
        if (nextPath.end.id == id && nextPath.index == p.index) {
          p.copy(pheromone = phi*p.pheromone + Q/pathDist)
        } else {
          p.copy(pheromone = phi*p.pheromone)
        }))
    }
  }
}
