package it.utiu.thesis.base

import it.utiu.thesis.base.AbstractStatsFeederActor.{AskStats, TellStats}

object AbstractStatsFeederActor {
  case class AskStats()

  case class TellStats(strCSV: String)
}

class AbstractStatsFeederActor extends AbstractBaseActor() {
  private val strCSV: String = null

  override def receive: Receive = onMessage(strCSV)

  private def onMessage(strCSV: String): Receive = {
    case AskStats() =>
      log.info("Stats requested")
      sender ! TellStats(strCSV)
    case AbstractAnalyzerActor.AnalysisFinished(result) =>
      log.info("Refreshed stats data just built")
      context.become(onMessage(result))
  }
}
