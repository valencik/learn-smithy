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

import cats.effect._

import learn.smithy.v04._
import pprint.PPrinter

object ProjectNestedTest extends IOApp.Simple {
  val pprint = PPrinter.Color

  val printer = Project.schema.compile(NestedPrinterSchemaVisitor)(Set.empty)._2
  val line = IO.println("+++++" * 10)
  val run = line *> IO.println(pprint(printer)) *> line
}

// Option 1
//
// authors: {
//  name: string
//  website: string
// }[]
// fakeauthors: {
//  name: string
//  website: string
// }[]

// Option 2
//
// authors: Author[]
// fakeauthors: Author[]
//
// Author {
//  name: string
//  website: string
// }
