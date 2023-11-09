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
    log.info("start analysis...")

    //invoke internal
    val stats = doInternalAnalysis(spark)
    //format csv
    val buff = new StringBuilder()
    //csv header
    buff.append(stats._1.mkString(",") + "\n")
    //csv values
    stats._2.foreach(row =>
      buff.append(row.toSeq.mkString(",") + "\n"))
    log.info("Stats computed:\n" + buff)

    //message to refresh feeder stats data
    context.actorSelection("/user/feeder-diabetes") ! AbstractAnalyzerActor.AnalysisFinished(buff.toString)

    //self-message to start a new training
    self ! AbstractAnalyzerActor.AnalysisFinished(buff.toString)
  }

  //(List[header_col], List[time, List[value]])
  def doInternalAnalysis(spark: SparkSession): (Array[String], List[Row])
}
