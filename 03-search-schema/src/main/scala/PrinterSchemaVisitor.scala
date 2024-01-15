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

trait PrinterSchemaVisitor[A] extends (Set[ShapeId] => (Set[ShapeId], String)) {}
object PrinterSchemaVisitor extends SchemaVisitor[PrinterSchemaVisitor] {
  def of[A](shapeId: ShapeId, msg: Option[String]): PrinterSchemaVisitor[A] =
    s => (s + shapeId, msg.getOrElse(""))

  def primitive[P](shapeId: ShapeId, hints: Hints, tag: Primitive[P]): PrinterSchemaVisitor[P] =
    of(shapeId, Some(s"primitive $tag"))

  def collection[C[_], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): PrinterSchemaVisitor[C[A]] =
    of(shapeId, Some("collection"))

  def map[K, V](
      shapeId: ShapeId,
      hints: Hints,
      key: Schema[K],
      value: Schema[V]
  ): PrinterSchemaVisitor[Map[K, V]] =
    of(shapeId, Some("map"))

  def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag[E],
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): PrinterSchemaVisitor[E] =
    of(shapeId, Some("enumeration"))

  def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[S, _]],
      make: IndexedSeq[Any] => S
  ): PrinterSchemaVisitor[S] =
    of(shapeId, Some(s"struct with ${fields.size} fields"))

  def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[U, _]],
      dispatch: Alt.Dispatcher[U]
  ): PrinterSchemaVisitor[U] =
    of(shapeId, Some(s"union with ${alternatives.size} cases"))

  def biject[A, B](schema: Schema[A], bijection: Bijection[A, B]): PrinterSchemaVisitor[B] = ???

  def refine[A, B](schema: Schema[A], refinement: Refinement[A, B]): PrinterSchemaVisitor[B] = ???

  def lazily[A](suspend: Lazy[Schema[A]]): PrinterSchemaVisitor[A] = ???

  def option[A](schema: Schema[A]): PrinterSchemaVisitor[Option[A]] = ???

}
