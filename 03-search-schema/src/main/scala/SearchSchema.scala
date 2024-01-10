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

import learn.smithy.v03._
import smithy4s.schema.Schema
import smithy4s.schema.Schema._
import smithy4s.Hints
import smithy4s.ShapeTag
import smithy4s.ShapeId
import smithy4s.schema.Field
import smithy4s.schema.SchemaPartition.NoMatch
import smithy4s.schema.SchemaPartition.SplittingMatch
import smithy4s.schema.SchemaPartition.TotalMatch
import smithy4s.PartialData

sealed trait SearchSchema[A]

object SearchSchema {

  final case class Empty[A](schema: Schema[A]) extends SearchSchema[A]
  final case class Fields[A](schema: Schema[A]) extends SearchSchema[A]
  final case class Split[A](
      matching: Schema[PartialData[A]],
      notMatching: Schema[PartialData[A]]
  ) extends SearchSchema[A]

  def apply[A](
      fullSchema: Schema[A]
  ): SearchSchema[A] = {
    fullSchema.partition(isSearchField) match {
      case NoMatch()                             => Empty(fullSchema)
      case SplittingMatch(matching, notMatching) => Split(matching, notMatching)
      case TotalMatch(schema)                    => Fields(schema)
    }
  }

  def isSearchField(field: Field[_, _]): Boolean = SearchSchemaBinding
    .fromHints(field.memberHints)
    .isDefined
}

sealed abstract class SearchSchemaBinding(val tpe: SearchSchemaBinding.Type)
    extends Product
    with Serializable {

  def show: String = this match {
    case SearchSchemaBinding.FieldTypeBinding(ft) => s"FieldType $ft"
  }
}
object SearchSchemaBinding extends ShapeTag.Companion[SearchSchemaBinding] {
  val id: ShapeId = ShapeId("learn.smithy.v03", "FieldTypeBinding")

  sealed trait Type
  object Type {
    case object FieldType extends Type
  }

  case class FieldTypeBinding(fieldType: String)
      extends SearchSchemaBinding(Type.FieldType)
  object FieldTypeBinding {
    val schema: Schema[FieldTypeBinding] =
      struct(string.required[FieldTypeBinding]("fieldType", _.fieldType))(
        FieldTypeBinding.apply
      )
  }

  implicit val schema: Schema[SearchSchemaBinding] = {
    val fieldType =
      FieldTypeBinding.schema.oneOf[SearchSchemaBinding]("fieldType")
    union(fieldType) { case _: FieldTypeBinding =>
      0
    }
  }

  def fromHints(
      // field: String,
      fieldHints: Hints
      // shapeHints: Hints
  ): Option[SearchSchemaBinding] = {
    val res = fieldHints
      .get(SearchOptions)
      .map(so => FieldTypeBinding(so.fieldType.name))
    println(s"+++ SearchSchema.fromHints, hints: $fieldHints")
    println(s"+++ SearchSchema.fromHints, result: $res")
    res
  }

}
