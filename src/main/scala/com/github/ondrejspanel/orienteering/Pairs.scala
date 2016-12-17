package com.github.ondrejspanel.orienteering

object Pairs {
  private val pairsTxt = io.Source.fromFile("pairs.csv").getLines

  val pairs = for (line <- pairsTxt.toList) yield {
    val fields = line.split(',')
    val name1 = fields(0)
    val name2 = fields(1)
    //val si2 = fields(2)
    (name1, name2)
  }
}
