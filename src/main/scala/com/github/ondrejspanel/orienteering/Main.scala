package com.github.ondrejspanel.orienteering

import java.io.{BufferedReader, InputStreamReader}
import java.sql.Timestamp

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl._
import org.squeryl.dsl.{CompositeKey2, ManyToMany, OneToMany}


object Main extends App with PrimitiveTypeMode {

  def time0: Long = 0
  type Time = Long

  val secret = getClass.getResourceAsStream("/secret.txt")

  val in = new BufferedReader(new InputStreamReader(secret))

  val dbName = in.readLine()
  val user = in.readLine()
  val password = in.readLine()

  object Race extends Schema {
    override def name = Some(dbName)

    override def tableNameFromClassName(tableName: String) = tableName.toLowerCase
    override def columnNameFromPropertyName(propertyName: String) = propertyName.toLowerCase

    val cards = table[Cards]
    val courses = table[Courses]
    val courseCodes = table[CourseCodes]
    val competitors = table[Competitors]
    val runs = table[Runs]
    val classDefs = table[ClassDefs]
    val codes = table[Codes]

    val courseRelation = oneToManyRelation(courses, courseCodes).via((c,cc) => c.id === cc.courseId)
    //val codeRelation = oneToManyRelation(courseCodes, codes).via((cc,c) => cc.codeId === c.id)
  }

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
  ) extends KeyedEntity[Int] {
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
  ) extends KeyedEntity[Int] {
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
  ) extends KeyedEntity[Int] {
    def this() = this(0)
  }

  class Courses(
    val id: Int,
    val name: String = "",
    val length: Option[Int] = Some(0),
    val climb: Option[Int] = Some(0),
    val note: Option[String] = Some("")
  ) extends KeyedEntity[Int] {
    def this() = this(0)

    lazy val courseCodes = Race.courseRelation.left(this)
  }

  class CourseCodes(
    val id: Int,
    val courseId: Int = 0,
    val position: Int = 0,
    val codeId: Int = 0
  ) extends KeyedEntity[Int] {
    def this() = this(0)

    //lazy val codes = Race.codeRelation.left(this)
  }

  class Codes(
    val id: Int,
    val code: Int = 0,
    val altCode: Option[Int] = Some(0),
    val outOfOrder: Boolean = false,
    val radio: Boolean = false,
    val note: Option[String] = Some("")
  ) extends KeyedEntity[Int] {
    def this() = this(0)
  }

  class ClassDefs(
    val id: Int,
    val classId: Int = 0,
    val stageId: Int = 0,
    val courseId: Int = 0,
    val startSlotIndex: Int = 0,
    val startTimeMin: Option[Int] = Some(0),
    val startIntervalMin: Option[Int] = Some(0),
    val vacantsBefore: Option[Int] = Some(0),
    val vacantEvery: Option[Int] = Some(0),
    val vacantsAfter: Option[Int] = Some(0),
    val mapCount: Option[Int] = Some(0),
    val resultsCount: Option[Int] = Some(0),
    val lastStartTimeMin: Option[Int] = Some(0),
    val drawLock: Boolean = false
  ) extends KeyedEntity[Int] {
    def this() = this(0)
  }

  def connectToDb(): Unit = {
    Class.forName("org.postgresql.Driver")

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/quickevent", user, password),
        new PostgreSqlAdapter))


    import Race._

    inTransaction {
      val cs = join(cards, runs, competitors, classDefs, courses)((c, r, p, d, course) =>
        select(c, r, p, d, course)
        on(c.runId === r.id, r.competitorId === p.id, p.classId === d.classId, d.courseId === course.id)
      )

      cs.foreach { case (card, run, person, classDef, course) =>
        println(card.punches)
        println(run.competitorId)
        println(person.firstName + " " + person.lastName)
        println(classDef)
        println(course)

        val cc = course.courseCodes
        cc.foreach { ccc =>
          println(ccc.codeId)
          //val ccq = ccc.codes
//          ccq.foreach { cq =>
//            println(cq.code)
//          }
        }
      }
    }
  }

  connectToDb()

}
