package org.apache.spark.ml.dsl.utils

import org.apache.spark.ml.dsl.AbstractFlowSuite

object XMLWeakDeserializerSuite {
  case class StrStr(
                     a: String,
                     b: String
                   )

  case class StrInt(
                     a: String = "A",
                     b: Int = 2
                   )

  case class StrDbl(
                     a: String,
                     b: Double
                   )

  case class StrIntArray(
                          a: String,
                          b: Array[Int]
                        )

  case class StrIntSeq(
                        a: String,
                        b: Seq[Int]
                      )

  case class StrIntSet(
                        a: String,
                        b: Set[Int]
                      )

  case class OptInt(
                     a: Option[Int]
                   )
}

class XMLWeakDeserializerSuite extends AbstractFlowSuite{

  implicit val formats = Xml.defaultFormats

  import XMLWeakDeserializerSuite._
  import org.json4s.Extraction._

  test("int to String") {

    val d1 = StrInt("a", 12)
    val json = decompose(d1)

    val d2 = extract[StrStr](json)
    d2.toString.shouldBe("StrStr(a,12)")
  }

  test("string to int") {
    val d1 = StrStr("a", "12")
    val json = decompose(d1)

    val d2 = extract[StrInt](json)
    d2.toString.shouldBe("StrInt(a,12)")
  }

  test("double to int") {
    val d1 = StrDbl("a", 12.51)
    val json = decompose(d1)

    val d2 = extract[StrInt](json)
    d2.toString.shouldBe("StrInt(a,12)")
  }

  test("int to int array") {
    val d1 = StrInt("a", 12)
    val json = decompose(d1)

    val d2 = extract[StrIntArray](json)
    d2.copy(b = null).toString.shouldBe("StrIntArray(a,null)")
  }

  test("int array to int array") {
    val d1 = StrIntArray("a", Array(12))
    val json = decompose(d1)

    val d2 = extract[StrIntArray](json)
    d2.copy(b = null).toString.shouldBe("StrIntArray(a,null)")
  }

  test("int to int seq") {
    val d1 = StrInt("a", 12)
    val json = decompose(d1)

    val d2 = extract[StrIntSeq](json)
    d2.toString.shouldBe("StrIntSeq(a,List(12))")
  }

  test("int to int set") {
    val d1 = StrInt("a", 12)
    val json = decompose(d1)

    val d2 = extract[StrIntSet](json)
    d2.toString.shouldBe("StrIntSet(a,Set(12))")
  }

  test("string to int array") {
    val d1 = StrStr("a", "12")
    val json = decompose(d1)

    val d2 = extract[StrIntArray](json)
    d2.copy(b = null).toString.shouldBe("StrIntArray(a,null)")
  }

  test("string to int seq") {
    val d1 = StrStr("a", "12")
    val json = decompose(d1)

    val d2 = extract[StrIntSeq](json)
    d2.toString.shouldBe("StrIntSeq(a,List(12))")
  }

  test("string to int set") {
    val d1 = StrStr("a", "12")
    val json = decompose(d1)

    val d2 = extract[StrIntSet](json)
    d2.toString.shouldBe("StrIntSet(a,Set(12))")
  }

  test("empty string to Object") {
    val d1 = ""
    val json = decompose(d1)

    val d2 = extract[OptInt](json)
    d2.toString.shouldBe("OptInt(None)")
  }

  test("empty string to Option[Map]") {
    val d1 = ""
    val json = decompose(d1)

    val d2 = extract[Option[Map[String, String]]](json)
    d2.toString.shouldBe("Some(Map())")
  }

  test("missing member to default constructor value") {
    val d1 = StrIntSeq("a", Nil)
    val json = decompose(d1)

    val d2 = extract[StrInt](json)
    d2.toString.shouldBe("StrInt(a,2)")
  }

  test("empty string to default constructor value") {
    val d1 = ""
    val json = decompose(d1)

    val d2 = extract[StrInt](json)
    d2.toString.shouldBe("StrInt(A,2)")
  }
}