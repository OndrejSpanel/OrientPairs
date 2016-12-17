package com.github.ondrejspanel.orienteering

import java.io.{BufferedReader, InputStreamReader}
import java.sql.Timestamp

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{PrimitiveTypeMode, Schema, Session, SessionFactory}


object Main extends App with PrimitiveTypeMode {

  class Card(
    val id: Int,
    val runid: Int,
    val runidassignts: Timestamp,
    val stageid: Int,
    val stationnumber: Int,
    val siid: Int,
    val checktime: Timestamp,
    val starttime: Timestamp,
    val finishtime: Timestamp,
    val punches: String,
    val readerconnectionid: Int,
    val printerconnectionid: Int
  )


  def connectToDb(): Unit = {
    Class.forName("org.postgresql.Driver")

    val secret = getClass.getResourceAsStream("/secret.txt")

    val in = new BufferedReader(new InputStreamReader(secret))

    val user = in.readLine()
    val password = in.readLine()

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/quickevent", user, password),
        new PostgreSqlAdapter))


    object Race extends Schema {
      val cards = table[Card]
    }

    inTransaction {
      val cs = from(Race.cards)(c =>
        where(c.runid === 3)
          select c
      )

      println(cs.toString)
    }
  }

  connectToDb()


}
