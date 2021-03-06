package com.tribbloids.spookystuff.pipeline.example.image

import com.tribbloids.spookystuff.SpookyContext
import com.tribbloids.spookystuff.dsl._
import com.tribbloids.spookystuff.example.QueryCore
import com.tribbloids.spookystuff.pipeline.google.ImageSearchTransformer
import org.apache.spark.rdd.RDD

/**
 * Created by peng on 10/06/14.
 */
object ListOfFeatures extends QueryCore {

  case class Features(
                       description: String,
                       list: String
                       )

  override def doMain(spooky: SpookyContext) = {

    import spooky.dsl._
    import spooky.sqlContext.implicits._

    val input = sc.parallelize(Seq(
      Features("Supported Browsers", "PhantomJS, HtmlUnit"),
      Features("Browser Actions", "Visit, Click, Drop Down Select, Text Input"),
      Features("Query Optimizers", "Narrow, Wide")
    )).toDF()
    .explode[String, String]("list","item")(_.split(",").map(v => "logo "+v.trim))

    val output: RDD[(Int, String, String)] = new ImageSearchTransformer().setInputCol("item").setImageUrisCol("uris")
      .transform(input)
      .select(x"""<div class="logo"><img src="${'uris.head}"/></div>""" ~ 'logo)
      .toPairRDD('description, 'logo)
      .groupByKey()
      .map {
        tuple =>
          (
            tuple._2.size,
            tuple._1.asInstanceOf[String],
            "%html " + tuple._2.mkString
            )
      }

    output.toDF()
  }
}