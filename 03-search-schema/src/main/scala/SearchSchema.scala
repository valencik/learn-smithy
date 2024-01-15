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
    // Split the Schema into parts, those with SearchOption annotations, and those without
    fullSchema.partition(isSearchField) match {
      case NoMatch()                             => Empty(fullSchema)
      case SplittingMatch(matching, notMatching) => Split(matching, notMatching)
      case TotalMatch(schema)                    => Fields(schema)
    }
  }

  def isSearchField(field: Field[_, _]): Boolean =
    SearchSchemaBinding.fromHints(field.label, field.memberHints).isDefined
}

sealed abstract class SearchSchemaBinding(val tpe: SearchSchemaBinding.Type)
    extends Product
    with Serializable {

  def show: String = this match {
    case SearchSchemaBinding.KeywordFieldTypeBinding(fn) => s"KeywordField $fn"
    case SearchSchemaBinding.TextFieldTypeBinding(fn)    => s"TextField $fn"
  }
}
object SearchSchemaBinding extends ShapeTag.Companion[SearchSchemaBinding] {
  val id: ShapeId = ShapeId("learn.smithy.v03", "SearchSchemaBinding")

  sealed trait Type
  object Type {
    case object KeywordFieldType extends Type
    case object TextFieldType extends Type
  }

  case class KeywordFieldTypeBinding(name: String)
      extends SearchSchemaBinding(Type.KeywordFieldType)
  object KeywordFieldTypeBinding {
    val schema: Schema[KeywordFieldTypeBinding] =
      struct(string.required[KeywordFieldTypeBinding]("name", _.name))(
        KeywordFieldTypeBinding.apply
      )
  }

  case class TextFieldTypeBinding(name: String) extends SearchSchemaBinding(Type.TextFieldType)
  object TextFieldTypeBinding {
    val schema: Schema[TextFieldTypeBinding] =
      struct(string.required[TextFieldTypeBinding]("name", _.name))(
        TextFieldTypeBinding.apply
      )
  }

  implicit val schema: Schema[SearchSchemaBinding] = {
    val keywordType =
      KeywordFieldTypeBinding.schema.oneOf[SearchSchemaBinding]("keywordField")
    val textType =
      TextFieldTypeBinding.schema.oneOf[SearchSchemaBinding]("textField")
    union(keywordType, textType) {
      case _: KeywordFieldTypeBinding => 0
      case _: TextFieldTypeBinding    => 1
    }
  }

  def fromHints(
      field: String,
      fieldHints: Hints
      // shapeHints: Hints
  ): Option[SearchSchemaBinding] = {
    val keyword = fieldHints
      .get(KeywordField)
      .map(f => KeywordFieldTypeBinding(f.name.getOrElse(field)))
    val text = fieldHints
      .get(TextField)
      .map(f => TextFieldTypeBinding(f.name.getOrElse(field)))
    val res = keyword orElse text
    println(s"+++ SearchSchema.fromHints($field), hints: $fieldHints")
    println(s"+++ SearchSchema.fromHints($field), result: $res")
    res
  }

}
