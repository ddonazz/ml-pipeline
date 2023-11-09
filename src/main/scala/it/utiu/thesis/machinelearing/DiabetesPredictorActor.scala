package it.utiu.thesis.machinelearing

import akka.actor.Props
import it.utiu.thesis.base.AbstractPredictorActor
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession

object DiabetesPredictorActor {
  def props(): Props = Props(new DiabetesPredictorActor())
}

class DiabetesPredictorActor() extends AbstractPredictorActor() {

  override def doInternalPrediction(messages: String, spark: SparkSession, model: Transformer): String = {
    val tokens = messages.split(",")
    val input = (tokens(0).toDouble, tokens(1).toDouble, tokens(2).toDouble, tokens(3).toDouble, tokens(4).toDouble, tokens(5).toDouble, tokens(6).toDouble, tokens(7).toDouble, tokens(8).toDouble, tokens(9).toDouble, tokens(10).toDouble, tokens(11).toDouble, tokens(12).toDouble, tokens(13).toDouble, tokens(14).toDouble, tokens(15).toDouble, tokens(16).toDouble, tokens(17).toDouble, tokens(18).toDouble, tokens(19).toDouble, tokens(20).toDouble)

    import spark.implicits._

    val sentenceData = Seq(input).toDF()
    val assembler = new VectorAssembler().setInputCols(Array("_1", "_2", "_3", "_4", "_5", "_6", "_7", "_8", "_9", "_10", "_11", "_12", "_13", "_14", "_15", "_16", "_17", "_18", "_19", "_20")).setOutputCol("features")
    val ds = assembler.transform(sentenceData)
    ds.show()

    val predictions = model.transform(ds)
    log.info("Diabetes prediction received from predictor actor:")
    predictions.show()
    val ret = predictions.select("prediction").collect().map(_(0)).toList

    ret.asInstanceOf[List[Double]].head.toString
  }

}
