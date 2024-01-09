package com.example

import learn.smithy._

import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import scala.concurrent.duration._
import smithy4s.http4s.SimpleRestJsonBuilder

object RepoServiceImpl extends RepoService[IO] {

  private val allRepos = List(Repo("cats"), Repo("cats-effect"))
  def repoSearch(
      name: Option[String]
  ): IO[SearchResult] = IO.pure {
    name match {
      case None        => SearchResult(allRepos)
      case Some(nameQ) => SearchResult(allRepos.filter(r => r.name == nameQ))
    }
  }
}

object Routes {
  private val repos: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(RepoServiceImpl).resource

  private val docs: HttpRoutes[IO] =
    smithy4s.http4s.swagger.docs[IO](RepoService)

  val all: Resource[IO, HttpRoutes[IO]] =
    repos.map(repos =>
      Logger.httpRoutes(logHeaders = false, logBody = true)(repos) <+> docs
    )
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
