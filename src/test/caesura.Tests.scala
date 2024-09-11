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

import probably.*
import gossamer.*
import rudiments.*
import anticipation.*
import spectacular.*
import contingency.*, strategies.throwUnsafely

given decimalizer: Decimalizer = Decimalizer(1)

object Tests extends Suite(t"Caesura tests"):
  def run(): Unit =
    suite(t"Parsing tests"):
      import dsvFormats.csv

      test(t"simple parse"):
        Dsv.parse(t"""hello,world""").rows.head
      .assert(_ == Row(t"hello", t"world"))

      test(t"simple parse with quotes"):
        Dsv.parse(t""""hello","world"""").rows.head
      .assert(_ == Row(t"hello", t"world"))

      test(t"empty unquoted field at start"):
        Dsv.parse(t",hello,world").rows.head
      .assert(_ == Row(t"", t"hello", t"world"))

      test(t"empty unquoted field at end"):
        Dsv.parse(t"hello,world,").rows.head
      .assert(_ == Row(t"hello", t"world", t""))

      test(t"empty unquoted field in middle"):
        Dsv.parse(t"hello,,world").rows.head
      .assert(_ == Row(t"hello", t"", t"world"))

      test(t"empty quoted field at start"):
        Dsv.parse(t""""","hello","world"""").rows.head
      .assert(_ == Row(t"", t"hello", t"world"))

      test(t"empty quoted field at end"):
        Dsv.parse(t""""hello","world",""""").rows.head
      .assert(_ == Row(t"hello", t"world", t""))

      test(t"empty quoted field in middle"):
        Dsv.parse(t""""hello","","world"""").rows.head
      .assert(_ == Row(t"hello", t"", t"world"))

      test(t"quoted comma"):
        Dsv.parse(t""""hello,world"""").rows.head
      .assert(_ == Row(t"hello,world"))

      test(t"escaped quotes"):
        Dsv.parse(t""""hello""world"""").rows.head
      .assert(_ == Row(t"""hello"world"""))

      test(t"multi-line CSV without trailing newline"):
        Dsv.parse(t"""foo,bar\nbaz,quux""").rows
      .assert(_ == LazyList(Row(t"foo", t"bar"), Row(t"baz", t"quux")))

      test(t"multi-line CSV with trailing newline"):
        Dsv.parse(t"""foo,bar\nbaz,quux\n""").rows
      .assert(_ == LazyList(Row(t"foo", t"bar"), Row(t"baz", t"quux")))

      test(t"multi-line CSV with CR and LF"):
        Dsv.parse(t"""foo,bar\r\nbaz,quux\r\n""").rows
      .assert(_ == LazyList(Row(t"foo", t"bar"), Row(t"baz", t"quux")))

      test(t"multi-line CSV with quoted newlines"):
        Dsv.parse(t""""foo","bar"\n"baz","quux"\n""").rows
      .assert(_ == LazyList(Row(t"foo", t"bar"), Row(t"baz", t"quux")))

      test(t"multi-line CSV with newlines and quotes in cells"):
        Dsv.parse(t""""f""oo","Hello\nWorld"\nbaz,"1\n2\n3\n"\n""").rows
      .assert(_ == LazyList(Row(t"f\"oo", t"Hello\nWorld"), Row(t"baz", t"1\n2\n3\n")))

      test(t"multi-line CSV with quoted quotes adjacent to newlines"):
        Dsv.parse(t""""f""oo","Hello\nWorld"\nbaz,"1""\n""2\n3\n"\n""").rows
      .assert(_ == LazyList(Row(t"f\"oo", t"Hello\nWorld"), Row(t"baz", t"1\"\n\"2\n3\n")))

      test(t"multi-line CSV with quoted quotes adjacent to open/close quotes"):
        Dsv.parse(t""""f""oo","${"\"\""}Hello\nWorld${t"\"\""}"\n""").rows
      .assert(_ == LazyList(Row(t"f\"oo", t"\"Hello\nWorld\"")))

    suite(t"Alternative formats"):
      test(t"Parse TSV data"):
        import dsvFormats.tsv
        Dsv.parse(t"Hello\tWorld\n").rows
      .assert(_ == LazyList(Row(t"Hello", t"World")))

      test(t"Parse TSV data"):
        import dsvFormats.tsvWithHeader
        Dsv.parse(t"Greeting\tAddressee\nHello\tWorld\n")
      .assert(_ == Dsv(LazyList(Row(IArray(t"Hello", t"World"), Map(t"Greeting" -> 0, t"Addressee" -> 1))), dsvFormats.tsvWithHeader))

    suite(t"Dynamic JSON access"):
      test(t"Access field by name"):
        import dsvFormats.tsvWithHeader
        import dynamicDsvAccess.enabled
        val dsv = Dsv.parse(t"greeting\taddressee\nHello\tWorld\n")
        dsv.rows.head.addressee
      .assert(_ == t"World")

    test(t"decode case class"):
      import dsvFormats.csv
      Dsv.parse(t"""hello,world""").rows.head.as[Foo]
    .assert(_ == Foo(t"hello", t"world"))

    test(t"decode complex case class"):
      import dsvFormats.csv
      Dsv.parse(t"""0.1,two,three,4,five,six""").rows.head.as[Bar]
    .assert(_ == Bar(0.1, Foo(t"two", t"three"), 4, Foo(t"five", t"six")))

    test(t"encode case class"):
      Foo(t"hello", t"world").dsv
    .assert(_ == Row(t"hello", t"world"))

    test(t"encode complex case class"):
      Bar(0.1, Foo(t"two", t"three"), 4, Foo(t"five", t"six")).dsv
    .assert(_ == Row(t"0.1", t"two", t"three", t"4", t"five", t"six"))

    test(t"convert simple row to string"):
      import dsvFormats.csv
      Dsv(LazyList(Row(t"hello", t"world"))).show
    .assert(_ == t"""hello,world""")

    test(t"convert complex row to string"):
      import dsvFormats.csv
      Dsv(LazyList(Row(t"0.1", t"two", t"three", t"4", t"five", t"six"))).show
    .assert(_ == t"""0.1,two,three,4,five,six""")

    test(t"convert row with escaped quote"):
      import dsvFormats.csv
      Dsv(LazyList(Row(t"hello\"world"))).show
    .assert(_ == t""""hello""world"""")

    test(t"simple parse TSV"):
      import dsvFormats.tsv
      Dsv.parse(t"hello\tworld")
    .assert(_ == Dsv(LazyList(Row(t"hello", t"world")), format = dsvFormats.tsv))

    test(t"decode case class from tsv"):
      import dsvFormats.tsv
      Dsv.parse(t"hello\tworld").rows.head.as[Foo]
    .assert(_ == Foo(t"hello", t"world"))

    test(t"convert case class to tsv"):
      import dsvFormats.tsv
      Seq(Foo(t"hello", t"world")).dsv.show
    .assert(_ == t"hello\tworld")

case class Foo(one: Text, two: Text)
case class Bar(one: Double, foo1: Foo, four: Int, foo2: Foo)