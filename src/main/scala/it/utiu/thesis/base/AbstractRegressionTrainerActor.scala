package it.utiu.thesis.base

import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.linalg.Matrix
import org.apache.spark.ml.stat.Correlation
import org.apache.spark.sql.{DataFrame, Row}

import java.nio.file.StandardOpenOption
import java.util.Date

abstract class AbstractRegressionTrainerActor extends AbstractTrainerActor {

  override def calculateMetrics(algo: String, predictions: DataFrame, rows: (Long, Long)): Double = {

    //print ml evaluation
    val evaluator = new RegressionEvaluator().setLabelCol("label").setPredictionCol("prediction").setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    log.info(s"$algo - Root mean squared error: $rmse")

    //print ml metrics
    evaluator.setMetricName("mse")
    val mse = evaluator.evaluate(predictions)
    log.info(s"$algo - Mean squared error: $mse")

    evaluator.setMetricName("r2")
    val r2 = evaluator.evaluate(predictions)
    log.info(s"$algo - r2: $r2")

    evaluator.setMetricName("mae")
    val mae = evaluator.evaluate(predictions)
    log.info(s"$algo - Mean absolute error: $mae")

    val str = dateFormat.format(new Date()) + "," + algo + "," + r2 + "," + rows._1 + "," + rows._2 + "\n"
    writeFile(RT_OUTPUT_PATH + "regression-eval.csv", str, Some(StandardOpenOption.APPEND))

    r2
  }

  protected def computeCorrelationMatrix(df: DataFrame): Unit = {
    //compute correlation matrix
    val Row(coeff1: Matrix) = Correlation.corr(df, "features").head
    println(s"Pearson correlation matrix:\n $coeff1")

    val Row(coeff2: Matrix) = Correlation.corr(df, "features", "spearman").head
    println(coeff2.toString(Int.MaxValue, Int.MaxValue))

    val buff = new StringBuilder("\n")
    for (v <- coeff2.colIter) {
      for (_ <- v.toArray) {
        buff.append(v.toArray.mkString(",") + "\n")
      }
    }
    writeFile(ANALYTICS_OUTPUT_FILE + ".corrMtx", buff.toString, None)
  }

}
