package it.utiu.thesis

import akka.actor.{ActorRef, ActorSystem}
import it.utiu.thesis.base.{AbstractConsumerActor, AbstractProducerActor, AbstractTrainerActor}
import it.utiu.thesis.machinelearing.{DiabetesPredictorActor, DiabetesTrainerActor}
import it.utiu.thesis.stream.{DiabetesConsumerActor, DiabetesProducerActor}

import java.util.Date
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps

object Runner {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("diabetes")
    println("Starting DIABETES at " + new Date() + "...")
    val app = new Runner(system)
    app.run()
  }
}

class Runner(system: ActorSystem) {
  private var trainerRef: ActorRef = _
  private var predictorRef: ActorRef = _
  private var consumerRef: ActorRef = _
  private var producerRef: ActorRef = _
  private var feederRef: ActorRef = _

  def run(): Unit = {
    trainerRef = system.actorOf(DiabetesTrainerActor.props(), "trainer-diabetes")
    predictorRef = system.actorOf(DiabetesPredictorActor.props(), "predictor-diabetes")
    consumerRef = system.actorOf(DiabetesConsumerActor.props(), "consumer-diabetes")
    producerRef = system.actorOf(DiabetesProducerActor.props(), "producer-diabetes")
    feederRef = system.actorOf(DiabetesProducerActor.props(), "feeder-diabetes")

    Thread.sleep(2000)
    consumerRef ! AbstractConsumerActor.StartConsuming()
    Thread.sleep(2000)
    producerRef ! AbstractProducerActor.StartProducing()
    Thread.sleep(2000)
    system.scheduler.scheduleOnce(0 minute) {
      trainerRef ! AbstractTrainerActor.StartTraining()
    }

    Await.ready(system.whenTerminated, Duration.Inf)
  }
}

