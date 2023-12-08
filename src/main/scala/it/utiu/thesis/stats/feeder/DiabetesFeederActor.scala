package it.utiu.thesis.stats.feeder

import akka.actor.Props
import it.utiu.thesis.base.AbstractStatsFeederActor

object DiabetesFeederActor {
  def props(): Props = Props(new DiabetesFeederActor())
}

class DiabetesFeederActor extends AbstractStatsFeederActor {
}
