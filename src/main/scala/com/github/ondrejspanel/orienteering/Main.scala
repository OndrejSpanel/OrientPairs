package com.github.ondrejspanel.orienteering

import java.io.{BufferedReader, InputStreamReader}
import java.sql.Timestamp

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{PrimitiveTypeMode, Schema, Session, SessionFactory}


object Main extends App with PrimitiveTypeMode {

  def time0: Long = 0
  type Time = Long

  class Cards(
    val id: Int,
    val runId: Int = 0,
    val runIdAssignts: Timestamp = new Timestamp(0),
    val stageId: Int = 0,
    val stationNumber: Int = 0,
    val siId: Int = 0,
    val checkTime: Time = time0,
    val startTime: Time = time0,
    val finishTime: Time = time0,
    val punches: String = "[]",
    val readerConnectionId: Int = 0,
    val printerConnectionId: Option[Int] = Some(0)
  ) {
    def this() = this(0)
  }

  class Runs(
    val id: Int,
    val competitorId: Int = 0,
    val siId: Int = 0,
    val stageId: Int = 0,
    val startTimeMs: Option[Time] = Some(time0),
    val finishTimeMs: Option[Time] = Some(time0),
    val timeMs: Time = time0,
    val offRace: Boolean = false,
    val notCompeting: Boolean = false,
    val disqualified: Boolean = false,
    val mispunch: Boolean = false,
    val badCheck: Boolean = false,
    val cardLent: Boolean = false,
    val cardReturned: Boolean = false
  ) {
    def this() = this(0)
  }

  class Competitors(
    val id: Int,
    val startNumber: Option[Int] = Some(0),
    val classId: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val registration: Option[String] = Some(""),
    val licence: Option[String] = Some(""),
    val club: Option[String] = Some(""),
    val country: Option[String] = Some(""),
    val siId: Int = 0,
    val note: Option[String] = Some(""),
    val ranking: Option[String] = Some(""),
    val importId: Option[String] = Some("")
  ) {
    def this() = this(0)
  }

  class Courses(
    val id: Int,
    val name: String = "",
    val length: Option[Int] = Some(0),
    val climb: Option[Int] = Some(0),
    val note: Option[String] = Some("")
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

      override def tableNameFromClassName(tableName: String) = tableName.toLowerCase
      override def columnNameFromPropertyName(propertyName: String) = propertyName.toLowerCase

      val cards = table[Cards]
      val courses = table[Courses]
      val competitors = table[Competitors]
      val runs = table[Runs]
    }

    import Race._

    inTransaction {
      val cs = join(cards, runs, competitors)((c, r, p) =>
        select(c, r, p)
        on(c.runId === r.id, r.competitorId === p.id)
      )

      cs.foreach { case (c, r, p) =>
        println(c.punches)
        println(r.competitorId)
        println(p.firstName + " " + p.lastName)
      }
    }
  }

  connectToDb()

}
