package it.utiu.thesis.base

import it.utiu.thesis.base.AbstractTrainerActor.{StartTraining, TrainingFinished}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.util.MLWritable
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ArrayBuffer

object AbstractTrainerActor {
  //start training message
  case class StartTraining()

  //finished training message
  case class TrainingFinished(model: Transformer)
}

abstract class AbstractTrainerActor extends AbstractBaseActor {

  initSpark("trainer", SPARK_URL_TRAINING)

  def calculateMetrics(algo: String, predictions: DataFrame, rows: (Long, Long)): Double

  override def receive: Receive = {

    case StartTraining() =>
      doTraining()

    case TrainingFinished(_: Transformer) =>
      log.info("training restart waiting...")
      Thread.sleep(AbstractBaseActor.LOOP_DELAY)
      log.info("restart training")
      doTraining()
  }

  private def doTraining(): Unit = {
    log.info("start training...")

    //invoke internal
    val evals = doInternalTraining(spark)

    //choose fittest model by r2/accuracy evaluator on regression/classification
    val metrics = ArrayBuffer[(Transformer, Double)]()
    for (eval <- evals) {
      val value = calculateMetrics(eval._1, eval._3, eval._4)
      metrics.append((eval._2, value))
    }
    val fittest = metrics.maxBy(_._2)._1

    //save ml model
    log.info("saving ml model into " + ML_MODEL_FILE + "...")
    fittest.asInstanceOf[MLWritable].write.overwrite().save(ML_MODEL_FILE)
    writeFile(ML_MODEL_FILE + ".algo", fittest.getClass.getName, None)
    log.info("saved ml model into " + ML_MODEL_FILE + "...")

    //notify predictor forcing model refresh
    context.actorSelection {
      "/user/predictor-diabetes"
    } ! TrainingFinished(fittest)

    //self-message to start a new training
    self ! AbstractTrainerActor.TrainingFinished(fittest)
  }

  def doInternalTraining(sc: SparkSession): List[(String, Transformer, DataFrame, (Long, Long))]

}
