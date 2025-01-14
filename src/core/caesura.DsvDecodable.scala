/*
    Caesura, version 0.26.0. Copyright 2025 Jon Pretty, Propensive OÜ.

    The primary distribution site is: https://propensive.com/

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
    file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions
    and limitations under the License.
*/

package caesura

import contingency.*
import denominative.*
import rudiments.*
import spectacular.*
import vacuous.*
import wisteria.*

import scala.compiletime.*

object DsvDecodable extends ProductDerivable[DsvDecodable]:
  class DsvProductDecoder[DerivationType](count: Int, lambda: Row => DerivationType)
  extends DsvDecodable:
    type Self = DerivationType
    override def width: Int = count
    def decode(row: Row): DerivationType = lambda(row)

  inline def join[DerivationType <: Product: ProductReflection]: DerivationType is DsvDecodable =
    val sum = contexts { [FieldType] => context => context.width }.sum
    var rowNumber: Ordinal = Prim
    var count = 0

    summonInline[Foci[CellRef]].give:
      DsvProductDecoder[DerivationType](sum, (row: Row) => construct:
        [FieldType] => context =>
          val index = row.columns.let(_.at(label)).or(count)
          val row2 = Row(row.data.drop(index))
          count += context.width
          focus(CellRef(rowNumber, label)):
            typeclass.decode(row2))

  given [ValueType: Decoder] => ValueType is DsvDecodable as decoder = _.data.head.decode[ValueType]

trait DsvDecodable:
  type Self
  def decode(elems: Row): Self
  def width: Int = 1
