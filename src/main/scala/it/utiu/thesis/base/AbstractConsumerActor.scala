package it.utiu.thesis.base

import akka.Done
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import it.utiu.thesis.base.AbstractConsumerActor.BUFF_SIZE
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import java.io.PrintWriter
import java.net.URI
import java.nio.file.StandardOpenOption
import java.util.Date
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Properties

object AbstractConsumerActor {
  //max buffered items to store
  val BUFF_SIZE = 5

  //start consuming message
  case class StartConsuming()
}

abstract class AbstractConsumerActor(topic: String, header: String) extends AbstractBaseActor {

  private val predictor = context.actorSelection("/user/predictor-diabetes")
  //buffered messages to store
  private val buffer = ArrayBuffer[String]()

  override def receive: Receive = {
    //start consuming message
    case AbstractConsumerActor.StartConsuming() =>
      doConsuming()


    //received prediction message
    case AbstractPredictorActor.TellPrediction(prediction, input) =>
      log.info("Received prediction: " + prediction)
      val txtOut = dateFormat.format(new Date()) + "," + input + "," + prediction + "\n"
      writeFile(RT_OUTPUT_FILE, txtOut, Some(StandardOpenOption.APPEND))

  }

  private def doConsuming(): Unit = {
    log.info("Start consuming for diabetes...")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val consumerSettings = ConsumerSettings(context.system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(AbstractBaseActor.KAFKA_BOOT_SVR)
      .withGroupId(AbstractBaseActor.KAFKA_GROUP_ID)

    val done =
      Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
        .mapAsync(1) { msg =>
          val strMsg = msg.value
          log.info(s"Received message value: $strMsg")
          val isPredictionReq = isPredictionRequest(strMsg)
          if (!isPredictionReq || isAlwaysInput) {
            //input for training action
            buffer.append(strMsg)
            if (buffer.size == BUFF_SIZE) {
              //dump data to HDFS
              log.info("Dump " + buffer.size + " input messages to HDFS")
              try {
                val path = new Path(HDFS_CS_INPUT_PATH + "diabetes.input." + new Date().getTime)
                val conf = new Configuration()
                val fs = FileSystem.get(new URI(AbstractBaseActor.HDFS_URL), conf)
                val out = fs.create(path)
                val pw = new PrintWriter(out)
                if (header != null & header.nonEmpty) pw.write(header + Properties.lineSeparator)
                buffer.foreach(i => pw.write(i + Properties.lineSeparator))
                pw.close()
                out.close()

              } catch {
                case t: Throwable => log.info(t.toString)
              }
              buffer.clear()
            } else log.info("input messages buffered")
          }
          if (isPredictionReq) {
            //input for prediction action
            log.info("request prediction for: " + strMsg)
            predictor ! AbstractPredictorActor.AskPrediction(strMsg)
          }
          Future.successful(Done)
        }
        .runWith(Sink.ignore)

    done.onComplete(_ => return)
  }

  //internal
  def isPredictionRequest(row: String): Boolean = false

  private def isAlwaysInput: Boolean = false

}
