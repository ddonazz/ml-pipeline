package it.utiu.thesis.base

import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import scala.collection.mutable.ArrayBuffer

abstract class AbstractClassificationTrainerActor extends AbstractTrainerActor {

  override def calculateMetrics(algo: String, predictions: DataFrame, rows: (Long, Long)): Double = {
    import predictions.sparkSession.implicits._
    val lp = predictions.select("label", "prediction")
    val countTotal = predictions.count()
    val correct = lp.filter($"label" === $"prediction").count()
    val wrong = lp.filter(not($"label" === $"prediction")).count()
    val ratioWrong = wrong.toDouble / countTotal.toDouble
    val ratioCorrect = correct.toDouble / countTotal.toDouble

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)

    val str = LocalDateTime.now().format(dateFormat) + "," + algo + "," + (accuracy + "," + countTotal + "," + correct + "," + wrong + "," + ratioWrong + "," + ratioCorrect) + "," + rows._1 + "," + rows._2 + "\n"
    writeFile(RT_OUTPUT_PATH + "classification-eval.csv", str, Some(StandardOpenOption.APPEND))

    accuracy
  }

  protected def computeConfusionMatrix(test: DataFrame): Unit = {
  val predictionAndLabels = test.rdd.map(row => (row.getAs[Double]("prediction"), row.getAs[Double]("label")))

  val metrics = new MulticlassMetrics(predictionAndLabels)

  println("Confusion matrix:")
  println(metrics.confusionMatrix)
  }
}
