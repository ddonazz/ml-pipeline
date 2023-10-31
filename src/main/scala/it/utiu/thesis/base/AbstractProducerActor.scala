package it.utiu.thesis.base

import java.nio.file.StandardWatchEventKinds._
import java.nio.file.{FileSystems, Path, Paths}
import scala.collection.JavaConverters._

object AbstractProducerActor {
  //start producing message
  case class StartProducing()
}

abstract class AbstractProducerActor extends AbstractBaseActor {

  override def receive: Receive = {
    case AbstractProducerActor.StartProducing() => doProduce()
  }

  private def doProduce(): Unit = {
    log.info("start producing for diabetes...")
    val watchService = FileSystems.getDefault.newWatchService()
    Paths.get(RT_INPUT_PATH).register(watchService, ENTRY_CREATE)

    while (true) {
      log.info("waiting new files from " + RT_INPUT_PATH + "...")
      val key = watchService.take()
      key.pollEvents().asScala.foreach(e => {
        e.kind() match {
          case ENTRY_CREATE =>
            val dir = key.watchable().asInstanceOf[Path]
            val fullPath = dir.resolve(e.context().toString)
            log.info("what service event received: [" + fullPath + "] created")
            elaborationFile(fullPath.toString)
          case _ =>
            println("?")
        }
      })
      key.reset()
      //wait x seconds before read again
      Thread.sleep(10000)
    }
  }

  private def elaborationFile(filePath: String): Unit = {
    Thread.sleep(500)
    log.info("process file " + filePath)
  }

}
