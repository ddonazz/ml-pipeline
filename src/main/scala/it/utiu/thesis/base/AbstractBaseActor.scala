package it.utiu.thesis.base

import akka.actor.{Actor, ActorLogging}
import it.utiu.thesis.base.AbstractBaseActor.HDFS_URL
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import java.nio.file.{Files, Paths, StandardOpenOption}
import java.text.SimpleDateFormat

object AbstractBaseActor {
  val HDFS_URL = "hdfs://localhost:9000/"
  val KAFKA_BOOT_SVR = "localhost:9092"
  val KAFKA_GROUP_ID = "group1"
  val LOOP_DELAY = 600000
}

abstract class AbstractBaseActor extends Actor with ActorLogging {
  val SPARK_URL_TRAINING: String = context.system.settings.config.getString("diabetes.spark.trainer")
  val SPARK_URL_PREDICTION: String = context.system.settings.config.getString("diabetes.spark.predictor")
  val SPARK_URL_ANALYSIS: String = context.system.settings.config.getString("diabetes.spark.analyzer")

  val HDFS_CS_PATH: String = HDFS_URL + "/diabetes/"
  val HDFS_CS_INPUT_PATH: String = HDFS_CS_PATH + "input/"

  val ML_MODEL_FILE = "./ml-model/diabetes/"
  val ML_MODEL_FILE_COPY = "./ml-model/diabetes_copy/"
  private val RT_PATH = "./rt/diabetes/"
  val RT_INPUT_PATH: String = RT_PATH + "input/"
  val RT_OUTPUT_PATH: String = RT_PATH + "output/"
  val RT_OUTPUT_FILE: String = RT_OUTPUT_PATH + "diabetes-prediction.csv"
  private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  val dateFormat = new SimpleDateFormat(DATE_FORMAT)
  var spark: SparkSession = _
  var sc: SparkContext = _
  var conf: SparkConf = _

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Pre restart diabetes for " + reason)
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Post restart diabetes for " + reason)
    super.postRestart(reason)
  }

  override def preStart(): Unit = {
    log.info("Pre start diabetes")
    super.preStart()
  }

  override def postStop(): Unit = {
    log.info("Post stop diabetes")
    super.postStop()
  }

  protected def initSpark(task: String, url: String): Unit = {
    conf = new SparkConf()
      .setAppName("diabetes-" + task).setMaster(url)
      .set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
      .set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem")

    spark = SparkSession.builder.config(conf).getOrCreate()

    sc = spark.sparkContext
    sc.setLogLevel("ERROR")
  }

  protected def writeFile(file: String, text: String, mode: Option[StandardOpenOption]): Unit = {
    val path = Paths.get(file)
    if (!Files.exists(path)) Files.createFile(path)
    if (mode.isDefined) Files.write(path, text.getBytes, mode.get) else Files.write(path, text.getBytes)
  }

}
