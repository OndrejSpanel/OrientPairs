package com.github.ondrejspanel.orienteering

import java.io.{BufferedReader, InputStreamReader}

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl._

object Main extends App with PrimitiveTypeMode {

  val secret = getClass.getResourceAsStream("/secret.txt")

  val in = new BufferedReader(new InputStreamReader(secret))

  val dbName = in.readLine()
  val user = in.readLine()
  val password = in.readLine()

  import Db._

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
    val codeRelation = oneToManyRelation(codes, courseCodes).via((c, cc) => cc.codeId === c.id)
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

        val missingCodes = {
          val correct = Util.lcs(card.codes, course.courseSeq)
          val expected = card.codes.length min course.courseSeq.length
          expected - correct.length
        }

        println(s"${person.firstName} ${person.lastName}: missing $missingCodes")

        val note = s"missing $missingCodes"

        update(competitors) ( p=>
          where(p.id ===  person.id)
          set(p.note := Some(note))
        )
      }
    }
  }

  connectToDb()

}
