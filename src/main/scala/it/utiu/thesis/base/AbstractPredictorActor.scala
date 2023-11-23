package it.utiu.thesis.base

import it.utiu.thesis.base.AbstractPredictorActor.{AskPrediction, TellPrediction}
import org.apache.commons.io.FileUtils
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, LogisticRegressionModel, RandomForestClassificationModel}
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, GBTRegressionModel, LinearRegressionModel, RandomForestRegressionModel}
import org.apache.spark.sql.SparkSession

import java.io.File
import java.nio.file.{Files, Paths}
import scala.io.Source.fromFile

object AbstractPredictorActor {
  case class AskPrediction(messages: String)

  case class TellPrediction(prediction: String, input: String)
}

abstract class AbstractPredictorActor extends AbstractBaseActor {

  private val mlModel: Transformer = null

  initSpark("predictor", SPARK_URL_PREDICTION)

  override def receive: Receive = onMessage(mlModel)

  def doInternalPrediction(messages: String, spark: SparkSession, model: Transformer): String

  private def getInput(msg: String): String = msg

  private def doPrediction(msg: String): String = {
    log.info("Start prediction...")

    if (mlModel == null) {
      if (!Files.exists(Paths.get(ML_MODEL_FILE))) return null
      context.become(onMessage(loadModelFromDisk()))
    }

    val prediction = doInternalPrediction(msg, spark, mlModel)

    prediction
  }

  private def loadModelFromDisk(): Transformer = {
    log.info("Restoring model " + ML_MODEL_FILE_COPY + " from disk...")
    FileUtils.deleteDirectory(new File(ML_MODEL_FILE_COPY))
    FileUtils.copyDirectory(new File(ML_MODEL_FILE), new File(ML_MODEL_FILE_COPY), true)
    val algo = fromFile(ML_MODEL_FILE + ".algo").getLines().next()
    algo match {
      case "org.apache.spark.ml.regression.LinearRegressionModel" => LinearRegressionModel.read.load(ML_MODEL_FILE_COPY)
      case "org.apache.spark.ml.regression.DecisionTreeRegressorModel" => DecisionTreeRegressionModel.read.load(ML_MODEL_FILE_COPY)
      case "org.apache.spark.ml.regression.RandomForestRegressionModel" => RandomForestRegressionModel.read.load(ML_MODEL_FILE_COPY)
      case "org.apache.spark.ml.regression.GBTRegressionModel" => GBTRegressionModel.read.load(ML_MODEL_FILE_COPY)
      case "org.apache.spark.ml.classification.LogisticRegressionModel" => LogisticRegressionModel.read.load(ML_MODEL_FILE_COPY)
      case "org.apache.spark.ml.classification.DecisionTreeClassificationModel" => DecisionTreeClassificationModel.read.load(ML_MODEL_FILE_COPY)
      case "org.apache.spark.ml.classification.RandomForestClassificationModel" => RandomForestClassificationModel.read.load(ML_MODEL_FILE_COPY)
    }
  }

  private def onMessage(mlModel: Transformer): Receive = {
    case AskPrediction(messages: String) =>
      val prediction = doPrediction(messages)
      if (prediction != null) sender ! TellPrediction(prediction, getInput(messages))

    case AbstractTrainerActor.TrainingFinished(model: Transformer) =>
      context.become(onMessage(model))
      log.info("Reloaded model " + mlModel + " just built")
  }
}
