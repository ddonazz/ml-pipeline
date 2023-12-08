package it.utiu.thesis.base

import it.utiu.thesis.base.AbstractAnalyzerActor.{AnalysisFinished, StartAnalysis}
import org.apache.spark.sql.{Row, SparkSession}

object AbstractAnalyzerActor {
  case class StartAnalysis()

  case class AnalysisFinished(strCSV: String)
}

abstract class AbstractAnalyzerActor() extends AbstractBaseActor() {
  initSpark("analyzer", SPARK_URL_ANALYSIS)

  override def receive: Receive = {

    case StartAnalysis() => doAnalysis()

    case AnalysisFinished(_) =>
      log.info("Analysis restart waiting...")
      Thread.sleep(AbstractBaseActor.LOOP_DELAY * 3)
      log.info("Restart analysis")
      doAnalysis()
  }

  private def doAnalysis(): Unit = {
    log.info("Start analysis...")

    val stats = doInternalAnalysis(spark)
    val buff = new StringBuilder()
    buff.append(stats._1.mkString(",") + "\n")
    stats._2.foreach(row =>
      buff.append(row.toSeq.mkString(",") + "\n"))
    log.info("Stats computed:\n" + buff)

    context.actorSelection("/user/feeder-diabetes") ! AbstractAnalyzerActor.AnalysisFinished(buff.toString)

    self ! AbstractAnalyzerActor.AnalysisFinished(buff.toString)
  }

  def doInternalAnalysis(spark: SparkSession): (Array[String], List[Row])
}
