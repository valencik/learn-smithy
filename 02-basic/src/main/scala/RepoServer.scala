/*
 * Copyright 2023 Andrew Valencik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example

import learn.smithy.v02._

import cats.effect._
import org.http4s._
import smithy4s.http4s.SimpleRestJsonBuilder

object ProjectServiceImpl extends ProjectService[IO] {

  private val allProjects = List(
    Project(
      "cats",
      "Abstractions for functional programming",
      "https://github.com/typelevel/cats",
      List(Platform.JS, Platform.JVM, Platform.NATIVE)
    ),
    Project(
      "cats-effect",
      "The pure asynchronous runtime for Scala",
      "https://github.com/typelevel/cats-effect",
      List(Platform.JS, Platform.JVM, Platform.NATIVE)
    )
  )
  def projectSearch(
      title: Option[String]
  ): IO[SearchResult] = IO.pure {
    title match {
      case None => SearchResult(allProjects)
      case Some(titleQ) =>
        SearchResult(allProjects.filter(r => r.title == titleQ))
    }
  }
}

object RepoServer extends MainServer(ProjectService) {
  val routes: Resource[IO, HttpRoutes[IO]] =
    SimpleRestJsonBuilder.routes(ProjectServiceImpl).resource
}
