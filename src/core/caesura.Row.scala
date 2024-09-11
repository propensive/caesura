/*
    Caesura, version [unreleased]. Copyright 2024 Jon Pretty, Propensive OÜ.

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

import anticipation.*
import gossamer.*
import rudiments.*
import spectacular.*

import scala.compiletime.*

case class Row(data: IArray[Text]):
  override def toString(): String = data.to(List).mkString("[", ";", "]")
  def as[CellType: DsvDecodable]: CellType = CellType.decode(this)

  override def hashCode: Int = data.indices.foldLeft(0): (aggregate, index) =>
    aggregate*31 + data(index).hashCode

  override def equals(that: Any): Boolean = that.asMatchable match
    case row: Row =>
      data.length == row.data.length && (data.indices.all { index => data(index) == row.data(index) })

    case _        => false

object Row:
  def apply(iterable: Iterable[Text]): Row = new Row(IArray.from(iterable))
  def apply(text: Text*): Row = new Row(IArray.from(text))

  given (using format: DsvFormat) => Row is Showable =
    _.data.map: cell =>
      if !cell.contains(format.Quote) then cell else
        Text.construct:
          append(format.quote)

          cell.s.foreach: char =>
            if char == format.quote then append(char)
            append(char)

          append(format.quote)
    .join(format.delimiter.show)