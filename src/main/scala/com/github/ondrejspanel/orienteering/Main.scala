package com.github.ondrejspanel.orienteering

import java.io.{BufferedReader, InputStreamReader}
import java.sql.Timestamp

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{PrimitiveTypeMode, Schema, Session, SessionFactory}


object Main extends App with PrimitiveTypeMode {

  def timeDefault: Long = 0
  type Time = Long

  class Cards(
    val id: Int,
    val runid: Int = 0,
    val runidassignts: Timestamp = new Timestamp(0),
    val stageid: Int = 0,
    val stationnumber: Int = 0,
    val siid: Int = 0,
    val checktime: Time = timeDefault,
    val starttime: Time = timeDefault,
    val finishtime: Time = timeDefault,
    val punches: String = "[]",
    val readerconnectionid: Int = 0,
    val printerconnectionid: Option[Int] = Some(0)
  ) {
    def this() = this(0)
  }


  def connectToDb(): Unit = {
    Class.forName("org.postgresql.Driver")

    val secret = getClass.getResourceAsStream("/secret.txt")

    val in = new BufferedReader(new InputStreamReader(secret))

    val dbName = in.readLine()
    val user = in.readLine()
    val password = in.readLine()

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/quickevent", user, password),
        new PostgreSqlAdapter))


    object Race extends Schema {
      override def name = Some(dbName)

      override def tableNameFromClassName(tableName: String) = NamingConventionTransforms.snakify(tableName)

      val cards = table[Cards]
    }

    inTransaction {
      val cs = from(Race.cards)(c =>
        where(c.runid === 3)
          select c
      )

      cs.foreach { c =>
        println(c.punches)
      }
    }
  }

  connectToDb()

}
