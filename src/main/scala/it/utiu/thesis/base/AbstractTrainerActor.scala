package it.utiu.thesis.base

import it.utiu.thesis.base.AbstractTrainerActor.{StartTraining, TrainingFinished}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.util.MLWritable
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ArrayBuffer

object AbstractTrainerActor {
  case class StartTraining()

  case class TrainingFinished(model: Transformer)
}

abstract class AbstractTrainerActor extends AbstractBaseActor {

  initSpark("trainer", SPARK_URL_TRAINING)

  def calculateMetrics(algo: String, predictions: DataFrame, rows: (Long, Long)): Double

  override def receive: Receive = {

    case StartTraining() =>
      doTraining()

    case TrainingFinished(_: Transformer) =>
      log.info("Training restart waiting...")
      Thread.sleep(AbstractBaseActor.LOOP_DELAY)
      log.info("Restart training")
      doTraining()
  }

  private def doTraining(): Unit = {
    log.info("Start training...")

    val evals = doInternalTraining(spark)

    val metrics = ArrayBuffer[(Transformer, Double)]()
    for (eval <- evals) {
      val value = calculateMetrics(eval._1, eval._3, eval._4)
      metrics.append((eval._2, value))
    }
    val fittest = metrics.maxBy(_._2)._1

    log.info("Saving ML Model into " + ML_MODEL_FILE + "...")
    fittest.asInstanceOf[MLWritable].write.overwrite().save(ML_MODEL_FILE)
    writeFile(ML_MODEL_FILE + ".algo", fittest.getClass.getName, None)
    log.info("Saved ML Model into " + ML_MODEL_FILE + "...")

    context.actorSelection {
      "/user/predictor-diabetes"
    } ! TrainingFinished(fittest)
    self ! AbstractTrainerActor.TrainingFinished(fittest)
  }

  def doInternalTraining(sc: SparkSession): List[(String, Transformer, DataFrame, (Long, Long))]

}
