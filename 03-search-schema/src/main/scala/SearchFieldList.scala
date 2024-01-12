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
import smithy4s.schema.Primitive.PString

// Modelling after the SchemaDescriptionDetailedImpl
// Which uses the following type parameter in the SchemaVisitor:
//  Set[ShapeId] => (Set[ShapeId], String)

// This shape basically allows us to track what `ShapeId`s we've seen before
// in order to catch and deal with recursion. We can "catch" recursion by checking if the `ShapeId`
// we're currently dealing with, is inside the `Set[ShapeId]` we're accumulating.
// The "output" type is really the `String` on the right hand side.

// So the following is a reasonable type parameter shape to start with:
//  Set[ShapeId] => (Set[ShapeId], A)
// And then vary the `A` based on what you're trying to build from the Schema.

final case class SearchField(name: String, tpe: String) {}
object SearchField {
  def fromHints[P](hints: Hints, primitive: Primitive[P]): SearchField =
    // TODO from the Hints get the name and type
    primitive match {
      case PString => SearchField("nameFromHint", "keyword")
      case _ =>
        SearchField(
          "nameFromHint",
          "bad, we don't support this"
        ) // we don't support anything but strings yet
    }
  def fromHints[E](hints: Hints, values: List[EnumValue[E]]): SearchField =
    // TODO from the Hints get the name and type
    SearchField("nameFromEnumHint", values.map(_.stringValue).mkString(","))
}

trait SearchFieldList[A]
    extends (Set[ShapeId] => (Set[ShapeId], List[SearchField])) {
  // helpers
  def mapResult[B](
      f: List[SearchField] => List[SearchField]
  ): SearchFieldList[B] = { seen =>
    val (s1, desc) = apply(seen)
    (s1 ++ seen, f(desc))
  }
}
object SearchFieldList extends SchemaVisitor[SearchFieldList] { self =>

  def of[A](shapeId: ShapeId, value: SearchField): SearchFieldList[A] =
    s => (s + shapeId, value :: Nil)
  def primitive[P](
      shapeId: ShapeId,
      hints: Hints,
      tag: Primitive[P]
  ): SearchFieldList[P] =
    SearchFieldList.of(shapeId, SearchField.fromHints(hints, tag))

  def collection[C[_], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): SearchFieldList[C[A]] =
    apply(member).mapResult(identity)

  def map[K, V](
      shapeId: ShapeId,
      hints: Hints,
      key: Schema[K],
      value: Schema[V]
  ): SearchFieldList[Map[K, V]] = ???

  def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag[E],
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): SearchFieldList[E] =
    SearchFieldList.of(shapeId, SearchField.fromHints(hints, values))

  def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[Field[S, _]],
      make: IndexedSeq[Any] => S
  ): SearchFieldList[S] = { seen =>
    // We're going to iterate over the `fields: Vector[Field[S, _]]` and call this
    def forField[T](
        sf: Field[S, T]
    ): (String, (Set[ShapeId], List[SearchField])) = {
      apply(sf.schema)(seen)
      sf.label -> apply(sf.schema)(seen)
    }
    val (shapesFinal, res) = fields
      // .foldLeft((Set.empty[ShapeId], Seq.empty[(String, List[SearchField])])) {
      .foldLeft((Set.empty[ShapeId], List.empty[SearchField])) {
        case ((shapes, fieldDesc), field) =>
          val (label, (s2, desc)) = forField(field)
          (shapes ++ s2, fieldDesc ++ desc)
      }
    (seen ++ shapesFinal, res)
  }

  def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[Alt[U, _]],
      dispatch: Alt.Dispatcher[U]
  ): SearchFieldList[U] = ???

  def biject[A, B](
      schema: Schema[A],
      bijection: Bijection[A, B]
  ): SearchFieldList[B] = ???

  def refine[A, B](
      schema: Schema[A],
      refinement: Refinement[A, B]
  ): SearchFieldList[B] = ???

  def lazily[A](suspend: Lazy[Schema[A]]): SearchFieldList[A] =
    // catch recursion here
    ???

  def option[A](schema: Schema[A]): SearchFieldList[Option[A]] = ???

}

//object SearchFieldList extends SchemaVisitor[FieldList] {
