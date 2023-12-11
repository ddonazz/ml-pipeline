package it.utiu.thesis.stream

import akka.actor.Props
import it.utiu.thesis.base.AbstractProducerActor
import it.utiu.thesis.utils.Constants

object DiabetesProducerActor {
  def props(): Props = Props(new DiabetesProducerActor)
}

class DiabetesProducerActor extends AbstractProducerActor(Constants.TOPIC_DIABETES) {
}
