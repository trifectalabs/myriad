// The MIT License (MIT)
// Copyright (c) 2016 Josiah Witt, Christopher Poenaru

package com.trifectalabs.myriad

import com.trifectalabs.myriad.aco.Path
import scala.concurrent.Future

trait Executor {
  def run: Future[Result]
}

case class Result(
  finalValue: Either[List[Double], List[Path]])

