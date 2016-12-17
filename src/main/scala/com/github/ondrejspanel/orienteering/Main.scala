package com.github.ondrejspanel.orienteering

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl._

object Main extends App with PrimitiveTypeMode {

  val secret = getClass.getResourceAsStream("/secret.txt")

  val in = io.Source.fromInputStream(secret).getLines

  val dbName = in.next()
  val user = in.next()
  val password = in.next()
  val missingPenalty = in.next().toInt

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
    val classes = table[Classes]

    val courseRelation = oneToManyRelation(courses, courseCodes).via((c,cc) => c.id === cc.courseId)
    val codeRelation = oneToManyRelation(codes, courseCodes).via((c, cc) => cc.codeId === c.id)
    val categoryRelation = oneToManyRelation(classes, competitors).via((cl, c) => c.classId === cl.id)
  }

  def connectToDb(): Unit = {
    Class.forName("org.postgresql.Driver")

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/quickevent", user, password),
        new PostgreSqlAdapter))


    import Race._

    case class Result(id: Int, category: String, timeSec: Long, missing: Int)

    val results = inTransaction {
      val cs = join(cards, runs, competitors, classDefs, courses)((c, r, p, d, course) =>
        select(c, r, p, d, course)
        on(c.runId === r.id, r.competitorId === p.id, p.classId === d.classId, d.courseId === course.id)
      )

      cs.map { case (card, run, person, classDef, course) =>

        val missingCodes = {
          val correct = Util.lcs(card.codes, course.courseSeq)
          val expected = card.codes.length min course.courseSeq.length
          expected - correct.length
        }

        val note = s"missing $missingCodes"

        update(competitors) ( p=>
          where(p.id ===  person.id)
          set(p.note := Some(note))
        )

        val fullName = person.lastName + " " + person.firstName
        fullName -> Result(person.id, person.category, run.timeMs / 1000 + missingCodes * missingPenalty, missingCodes)
      }.toMap
    }

    val pairs = Pairs.pairs

    val pairResults = for {
      (name1, name2) <- pairs
      r1 <- results.get(name1)
      r2 <- results.get(name2)
    } yield {
      (name1, name2, r1.category, r1.timeSec + r2.timeSec, r1.missing + r2.missing)
    }

    for (r <- pairResults) {
      println(r.productIterator.mkString(","))
    }
  }

  connectToDb()

}
