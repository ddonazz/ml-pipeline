package it.utiu.thesis.machinelearing

import akka.actor.Props
import it.utiu.thesis.base.AbstractClassificationTrainerActor
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.classification.{DecisionTreeClassifier, LogisticRegression, RandomForestClassifier}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object DiabetesTrainerActor {
  def props(): Props = Props(new DiabetesTrainerActor())
}

class DiabetesTrainerActor extends AbstractClassificationTrainerActor {

  override def doInternalTraining(spark: SparkSession): List[(String, Transformer, DataFrame, (Long, Long))] = {

    val df1 = spark.read.format("csv")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(HDFS_CS_PATH + "*")
      .toDF("_1", "_2", "_3", "_4", "_5", "_6", "_7", "_8", "_9", "_10", "_11", "_12", "_13", "_14", "_15", "_16", "_17", "_18", "_19", "_20", "_21", "_22")
      .withColumn("label", col("_1"))
    df1.show

    val assembler = new VectorAssembler()
      .setInputCols(Array("_2", "_3", "_4", "_5", "_6", "_7", "_8", "_9", "_10", "_11", "_12", "_13", "_14", "_15", "_16", "_17", "_18", "_19", "_20", "_21", "_22"))
      .setOutputCol("features")
    val df2 = assembler.transform(df1)

    val splitSeed = new Random().nextInt()
    val Array(trainingData, testData) = df2.randomSplit(Array(0.75, 0.25), splitSeed)
    val trainCount = trainingData.count()
    val testCount = testData.count()
    println("Training count:" + trainCount)
    println("Test count:" + testCount)

    val eval = ArrayBuffer[(String, Transformer, DataFrame, (Long, Long))]()

    //LOGISTIC REGRESSION CLASSIFIER
    val lr = new LogisticRegression()
      .setRegParam(0.01)
      .setElasticNetParam(0.7)
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setFamily("multinomial")

    val modelLR = lr.fit(trainingData)
    val predictionsLR = modelLR.transform(testData)
    eval.append(("LogisticRegression", modelLR, predictionsLR, (trainCount, testCount)))

    computeConfusionMatrix(predictionsLR)

    //DECISION TREES CLASSIFIER
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setMinInfoGain(3)

    val modelDT = dt.fit(trainingData)
    val predictionsDT = modelDT.transform(testData)
    eval.append(("DecisionTreeClassifier", modelDT, predictionsDT, (trainCount, testCount)))

    computeConfusionMatrix(predictionsDT)

    //RANDOM FOREST CLASSIFIER
    val rf = new RandomForestClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setNumTrees(100)
      .setMinInfoGain(3)

    val modelRF = rf.fit(trainingData)
    val predictionsRF = modelRF.transform(testData)
    eval.append(("RandomForestClassifier", modelRF, predictionsRF, (trainCount, testCount)))

    computeConfusionMatrix(predictionsRF)

    eval.toList
  }
}
