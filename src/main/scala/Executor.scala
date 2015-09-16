package com.trifectalabs.myriad

import scala.concurrent.Future

trait Executor {
  def run: Future[Result]
}

case class Result(
  finalValue: AnyVal 
)

