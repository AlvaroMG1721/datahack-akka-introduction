package com.datahack.akka.introduction

import akka.actor.{ActorRef, ActorSystem, Props}
import com.datahack.akka.introduction.actors.Student.PerformAnAdviceRequest
import com.datahack.akka.introduction.actors.{Student, Teacher}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object Boot extends App {

  // Initialize the ActorSystem
  val actorSystem = ActorSystem("UniversityMessageSystem")

  // Whe need an execution context to perform the concurrent code
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  // Construct the Actor Refs
  val teacherActorRef: ActorRef = actorSystem.actorOf(Props[Teacher], "teacher")
  val studentActorRef: ActorRef = actorSystem.actorOf(Props(classOf[Student], teacherActorRef), "student")

  actorSystem.scheduler.schedule(
    5 seconds,
    15 seconds,
    studentActorRef,
    PerformAnAdviceRequest
  )

  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook(actorSystem.terminate())

}
