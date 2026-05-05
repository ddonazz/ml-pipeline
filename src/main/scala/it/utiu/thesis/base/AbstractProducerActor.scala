package it.utiu.thesis.base

import akka.NotUsed
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import scala.util.{Try, Success, Failure}

import java.nio.file.StandardWatchEventKinds._
import java.nio.file.{FileSystems, Path, Paths}
import scala.collection.JavaConverters._

object AbstractProducerActor {
  //start producing message
  case class StartProducing()
}

abstract class AbstractProducerActor(topic: String) extends AbstractBaseActor {

  override def receive: Receive = {
    case AbstractProducerActor.StartProducing() => doProduce()
  }

  private def doProduce(): Unit = {
    log.info("Start producing for diabetes...")
    val watchService = FileSystems.getDefault.newWatchService()
    Paths.get(RT_INPUT_PATH).register(watchService, ENTRY_CREATE)

    while (true) {
      log.info("Waiting new files from " + RT_INPUT_PATH + "...")
      val key = watchService.take()
      key.pollEvents().asScala.foreach(e => {
        e.kind() match {
          case ENTRY_CREATE =>
            val dir = key.watchable().asInstanceOf[Path]
            val fullPath = dir.resolve(e.context().toString)
            log.info("What service event received: [" + fullPath + "] created")
            elaborationFile(fullPath.toString)
          case _ =>
            println("?")
        }
      })
      key.reset()
      Thread.sleep(10000)
    }
  }

  private def elaborationFile(filePath: String): Unit = {
    Thread.sleep(500)
    log.info("Process file " + filePath)

    implicit val actorMaterialize: ActorMaterializer = ActorMaterializer()
    val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(AbstractBaseActor.KAFKA_BOOT_SVR)

  val source: Source[String, NotUsed] = Try {
      val file = scala.io.Source.fromFile(filePath)
      Source.fromIterator(() => file.getLines())
    } match {
      case Success(src) => src
      case Failure(ex) =>
        log.error("Error reading file", ex)
        return
    }
    source
      .map { elem =>
        new ProducerRecord[Array[Byte], String](topic, elem)
      }
      .runWith(Producer.plainSink(producerSettings))
  }
}
