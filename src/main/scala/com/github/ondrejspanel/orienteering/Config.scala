package com.github.ondrejspanel.orienteering
import scala.collection.immutable.Seq

object Config {

  val pairs: Seq[(String, String)] = {
    val pairsTxt = io.Source.fromFile("pairs.csv")("UTF-8").getLines
    for (line <- pairsTxt.toList) yield {
      val fields = line.split(',')
      val name1 = fields(0)
      val name2 = fields(1)
      //val si2 = fields(2)
      (name1, name2)
    }
  }

  private val categories: Map[String, String] = {
    val catsTxt = io.Source.fromFile("categories.csv")("UTF-8").getLines

    catsTxt.toList.flatMap { line =>
      val fields = line.split(',')
      val cat = fields(0)

      fields.map(f => f -> cat)
    }.toMap

  }

  def mapCategory(cat: String): String = categories.getOrElse(cat, cat)

}
