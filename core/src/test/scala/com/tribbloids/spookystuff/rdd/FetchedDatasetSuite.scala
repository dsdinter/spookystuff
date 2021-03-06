package com.tribbloids.spookystuff.rdd

import com.tribbloids.spookystuff.actions._
import com.tribbloids.spookystuff.{SpookyEnvFixture, dsl}

/**
  * Created by peng on 5/10/15.
  */
class FetchedDatasetSuite extends SpookyEnvFixture {

  import dsl._

  //    test("should support repartition") {
  //      val spooky = this.spooky
  //
  //      sc.setCheckpointDir(s"file://${System.getProperty("user.dir")}/temp/spooky-unit/${this.getClass.getSimpleName}/")
  //
  //      val first = spooky
  //        .fetch(Wget("http://en.wikipedia.org")).persist()
  //      first.checkpoint()
  //      first.count()
  //
  //      val second = first.wgetJoin(S"a".hrefs, joinType = LeftOuter)
  //        .extract(S.uri ~ 'uri)
  //        .repartition(14)
  //
  //      val result = second.collect()
  //      result.foreach(println)
  //
  //      assert(result.length == 2)
  //      assert(first.spooky.metrics.pagesFetched.value == 2)
  //    }

  test(s".map should not run preceding transformation multiple times") {
    val acc = sc.accumulator(0)

    val set = spooky
      .fetch(
        Wget(HTML_URL)
      )
      .map{
        v =>
          acc += 1
          v
      }
    assert(acc.value == 0)

    //    val rdd = set.rdd
    //    assert(acc.value == 0)

    set.count()
    assert(acc.value == 1)
  }

  test(s".rdd should not run preceding transformation multiple times") {
    val acc = sc.accumulator(0)

    val set = spooky
      .fetch(
        Wget(HTML_URL)
      )
      .extract(
        S.andOptionFn{
          page =>
            acc += 1
            page.saved.headOption
        } ~ 'path
      )
    assert(acc.value == 0)

    val rdd = set.squashedRDD
    assert(acc.value == 0)

    rdd.count()
    assert(acc.value == 1)
  }

  // if not adding up to 1, this is the debugging method:
  // 1. add breakpoint on accumulator, execute to it >1 times and dump a memory snapshot
  // 2. compare stacktrace of executor thread on both snapshots
  for (sort <- Seq(false, true)) {
    test(s"toDF($sort) should not run preceding transformation multiple times") {
      val acc = sc.accumulator(0)

      val set = spooky
        .fetch(
          Wget(HTML_URL)
        )
        .extract(
          S.andOptionFn{
            page =>
              acc += 1
              page.saved.headOption
          } ~ 'path
        )
      assert(acc.value == 0)

      val df = set.toDF(sort)
      //      assert(acc.value == 0)

      df.count()
      assert(acc.value == 1)
    }

    test(s"toJSON($sort) should not run preceding transformation multiple times") {
      val acc = sc.accumulator(0)

      val set = spooky
        .fetch(
          Wget(HTML_URL)
        )
        .extract(
          S.andOptionFn{
            page =>
              acc += 1
              page.saved.headOption
          } ~ 'path
        )
      assert(acc.value == 0)

      val json = set.toJSON(sort)
      //      assert(acc.value == 0)

      json.count()
      assert(acc.value == 1)
    }

    test(s"toMapRDD($sort) should not run preceding transformation multiple times") {
      val acc = sc.accumulator(0)

      val set = spooky
        .fetch(
          Wget(HTML_URL)
        )
        .select(
          S.andOptionFn{
            page =>
              acc += 1
              page.saved.headOption
          } ~ 'path
        )
      assert(acc.value == 0)

      val rdd = set.toMapRDD(sort)
      //      assert(acc.value == 0)

      rdd.count()
      assert(acc.value == 1)
    }
  }

  test("toDF can yield a DataFrame") {

    val set = spooky
      .fetch(
        Wget(HTML_URL)
      )
      .select(
        S.uri ~ 'uri,
        S.children("h1").size ~ 'size,
        S.timestamp ~ 'timestamp,
        S.andOptionFn{
          page =>
            page.saved.headOption
        } ~ 'saved
      )
    val df = set.toDF()

    df.schema.treeString.shouldBe(
      """
        |root
        | |-- uri: string (nullable = true)
        | |-- size: integer (nullable = true)
        | |-- timestamp: timestamp (nullable = true)
        | |-- saved: string (nullable = true)
      """.stripMargin
    )

    df.show(false)
  }

  test("toDF can yield a DataFrame excluding Fields with .isSelected = false") {

    val set = spooky
      .fetch(
        Wget(HTML_URL)
      )
      .select(
        S.uri ~ 'uri.*,
        S.children("h1").size ~ 'size.*,
        S.timestamp ~ 'timestamp,
        S.andOptionFn{
          page =>
            page.saved.headOption
        } ~ 'saved
      )
    val df = set.toDF()

    df.schema.treeString.shouldBe(
      """
        |root
        | |-- timestamp: timestamp (nullable = true)
        | |-- saved: string (nullable = true)
      """.stripMargin
    )

    df.show(false)
  }
}
