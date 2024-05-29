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

import smithy4s.schema.Schema
import smithy4s.Hints
import smithy4s.ShapeId
import smithy4s.schema.Field
import smithy4s.schema.SchemaVisitor
import smithy4s.Refinement
import smithy4s.schema.Alt
import smithy4s.schema.Primitive
import smithy4s.schema.CollectionTag
import smithy4s.Bijection
import smithy4s.Lazy
import smithy4s.schema.{EnumTag, EnumValue}

sealed trait TSField {
  def name: String
  def isRequired: Boolean
}
case class TSPrim(name: String, isRequired: Boolean, tpe: String) extends TSField
case class TSArray(name: String, isRequired: Boolean, member: TSField) extends TSField
case class TSStruct(name: String, isRequired: Boolean, fields: Vector[TSField]) extends TSField

sealed trait TSFieldB {
  def isRequired: Boolean
  def name: Option[String]

  def setRequired(isRequired: Boolean): TSFieldB
  def setName(name: String): TSFieldB
}
case class TSPrimB(name: Option[String], isRequired: Boolean, tpe: String) extends TSFieldB {
  def setRequired(isRequired: Boolean): TSFieldB = this.copy(isRequired = isRequired)
  def setName(name: String): TSFieldB = this.copy(name = Some(name))
}
case class TSArrayB(name: Option[String], isRequired: Boolean, member: TSFieldB) extends TSFieldB {
  def setRequired(isRequired: Boolean): TSFieldB = this.copy(isRequired = isRequired)
  def setName(name: String): TSFieldB = this.copy(name = Some(name))
}
case class TSStructB(name: Option[String], isRequired: Boolean, fields: Vector[TSFieldB])
    extends TSFieldB {
  def setRequired(isRequired: Boolean): TSFieldB = this.copy(isRequired = isRequired)
  def setName(name: String): TSFieldB = this.copy(name = Some(name))
}

trait NestedPrinterSchemaVisitor[A] extends (Set[ShapeId] => (Set[ShapeId], TSFieldB)) {}
object NestedPrinterSchemaVisitor extends SchemaVisitor[NestedPrinterSchemaVisitor] {

  def primitive[P](
      shapeId: ShapeId,
      hints: Hints,
      tag: Primitive[P]
  ): NestedPrinterSchemaVisitor[P] = { seen =>
    (seen + shapeId, TSPrimB(None, true, tag.toString()))
  }

  def collection[C[_], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): NestedPrinterSchemaVisitor[C[A]] = { seen =>
    {
      val (newSeen, m) = apply(member)(seen + shapeId)
      (newSeen, TSArrayB(None, true, m))
    }
  }

  def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[S, _]],
      make: IndexedSeq[Any] => S
  ): NestedPrinterSchemaVisitor[S] = { seen =>
    {
      var allShapes = seen + shapeId
      val fs = fields.map(f => {
        // TODO should this be a foldLeft or something where we accumulate seen things?
        val (newSeen, fb) = apply(f.schema)(seen + shapeId)
        allShapes = allShapes ++ newSeen
        fb.setName(f.label)
      })
      (allShapes, TSStructB(Some(shapeId.name), true, fs))
    }
  }

  def option[A](schema: Schema[A]): NestedPrinterSchemaVisitor[Option[A]] = { seen =>
    {
      val (newSeen, f) = apply(schema)(seen)
      (newSeen, f.setRequired(false))
    }
  }

  // --- unimplemented ---

  def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag[E],
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): NestedPrinterSchemaVisitor[E] =
    ???

  def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[U, _]],
      dispatch: Alt.Dispatcher[U]
  ): NestedPrinterSchemaVisitor[U] =
    ???

  def map[K, V](
      shapeId: ShapeId,
      hints: Hints,
      key: Schema[K],
      value: Schema[V]
  ): NestedPrinterSchemaVisitor[Map[K, V]] =
    ???

  def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): NestedPrinterSchemaVisitor[B] =
    ???

  def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): NestedPrinterSchemaVisitor[B] =
    ???

  def lazily[A](suspend: Lazy[Schema[A]]): NestedPrinterSchemaVisitor[A] = ???

}
