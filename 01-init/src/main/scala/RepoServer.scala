package com.example

import learn.smithy._

import cats.effect._
import org.http4s._
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

object RepoServer extends MainServer(RepoService) {
  val routes: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(RepoServiceImpl).resource
}
