package com.github.ondrejspanel.orienteering

object Pairs {
  private val pairsTxt = io.Source.fromFile("pairs.csv").getLines

  case class Name(first: String, last: String)
  case class Pair(name1: Name, name2: Name)

  val pairs = for (line <- pairsTxt.toList) yield {
    val fields = line.split(',')
    val name1 = fields(0).split(' ')
    val name2 = fields(1).split(' ')
    //val si2 = fields(2)
    Pair(Name(name1(0), name1(1)), Name(name2(0), name2(1)))
  }
}
