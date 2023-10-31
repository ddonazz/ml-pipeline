package it.utiu.thesis.stream

import akka.actor.Props
import it.utiu.thesis.base.AbstractProducerActor

object DiabetesProducerActor {
  def props(): Props = Props(new DiabetesProducerActor)
}

class DiabetesProducerActor extends AbstractProducerActor() {
}
