package com.example

import hello._
import cats.syntax.all._
import cats.effect._
import cats.effect.syntax.all._
import org.http4s.implicits._
import org.http4s.ember.server._
import org.http4s._
import com.comcast.ip4s._
import smithy4s.http4s.SimpleRestJsonBuilder
import scala.concurrent.duration._

object HelloWorldImpl extends HelloWorldService[IO] {
  def hello(
      name: String,
      town: Option[List[String]],
      tags: Option[Map[String, List[String]]]
  ): IO[Greeting] = IO.pure {
    val tagStr = tags
      .map(m => m.toList.map(kv => s"${kv._1}: ${kv._2}").mkString(", "))
      .getOrElse("")
    val msg = town match {
      case None    => s"Hello " + name + "!"
      case Some(t) => s"Hello " + name + " from " + t + "!"
    }
    tags match {
      case None        => Greeting(msg)
      case Some(value) => Greeting(s"$msg  $tagStr")
    }
  }
}

object Routes {
  private val example: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(HelloWorldImpl).resource

  private val docs: HttpRoutes[IO] =
    smithy4s.http4s.swagger.docs[IO](HelloWorldService)

  val all: Resource[IO, HttpRoutes[IO]] = example.map(_ <+> docs)
}

object Main extends IOApp.Simple {
  val run = Routes.all
    .flatMap { routes =>
      val thePort = port"9000"
      val theHost = host"localhost"
      val message =
        s"Server started on: $theHost:$thePort, press enter to stop"

      EmberServerBuilder
        .default[IO]
        .withPort(thePort)
        .withHost(theHost)
        .withHttpApp(routes.orNotFound)
        .withShutdownTimeout(1.second)
        .build
        .productL(IO.println(message).toResource)
    }
    .surround(IO.readLine)
    .void
    .guarantee(IO.println("Goodbye!"))

}
