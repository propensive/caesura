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

package dsvFormats:
  given DsvFormat as csv = DsvFormat(false, ',', '"', '"')
  given DsvFormat as csvWithHeadings = DsvFormat(true, ',', '"', '"')
  given DsvFormat as tsv = DsvFormat(false, '\t', '"', '"')
  given DsvFormat as tsvWithHeadings = DsvFormat(true, '\t', '"', '"')
  given DsvFormat as ssv = DsvFormat(false, ' ', '"', '"')
  given DsvFormat as ssvWithHeadings = DsvFormat(true, ' ', '"', '"')

extension [ValueType: DsvEncodable](value: ValueType) def dsv: Row = ValueType.encode(value)

extension [ValueType: DsvEncodable](value: Seq[ValueType])
  def dsv: Dsv = Dsv(value.to(LazyList).map(ValueType.encode(_)))