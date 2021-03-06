package com.github.oopman.collectioneer.server.data

import com.github.oopman.collectioneer.server.Config
import com.github.oopman.collectioneer.server.data.Models.Tag
import io.getquill.NamingStrategy
import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.idiom.SqlIdiom

class TagObjects[Dialect <: SqlIdiom, Naming <: NamingStrategy](override val context: JdbcContext[Dialect, Naming]) extends Objects(context) {
  import context._

  /**
    * Retrieve a single Tag by name
    *
    * @param name
    * @return
    */
  def getTag(id: Int): Option[Tag] = context.run(query[Tag].filter(_.id == lift(id))) match {
    case List(tag) => Some(tag)
    case Nil => None
  }

  /**
    * Retrieve a Sequence of Tags constrained by various input parameters
    *
    * @param offset Offset within Tags dataset to retrieve
    * @param limit Number of Tags to retrieve
    * @return A Sequence of Tags
    */
  def getTags(categoryId: Option[Option[Int]]=None,
              offset: Int=Config.defaultOffset,
              limit: Int=Config.defaultLimit): Seq[Tag] = {
    val tagsQuery = quote {
      query[Tag].drop(lift(offset)).take(lift(limit))
    }
    val finalTagsQuery = categoryId match {
      case Some(optionalCategoryId) => optionalCategoryId match {
        case Some(actualCategoryId) => quote {
          tagsQuery.filter(_.categoryId.contains(lift(actualCategoryId)))
        }
        case None => quote {
          tagsQuery.filter(_.categoryId.isEmpty)
        }
      }
      case None => tagsQuery
    }
    context.run(finalTagsQuery)
  }

  /**
    * Insert a Tag object and returns the ID of the newly created Tag
    *
    * @param name Name of the Tag. This is the primary key and thus must be unique within the dataset
    * @param categoryId Optional. ID of the Category to associate the Tag with
    * @param data Optional. Additional data associated with the Tag
    * @return
    */
  def insertTag(name: String,
                categoryId: Option[Int]=None,
                data: Option[String]=None): Long = {
    context.run(query[Tag].insert(
      _.name -> lift(name),
      _.categoryId -> lift(categoryId),
      _.data -> lift(data)
    ))
  }

  /**
    * Update a Tag object, returning its ID in the process
    *
    * @param tag A Tag instance
    * @return
    */
  def updateTag(tag: Tag): Long = {
    context.run(query[Tag].filter(_.id == lift(tag.id)).update(lift(tag)))
  }

  /**
    * Delete a Tag object. Will fail if any TagCollectionAssn or TagItemAssn
    * objects associated with the Tag exists
    *
    * @param id ID of Tag to delete
    * @return
    */
  def deleteTag(id: Int): Long = {
    context.run(query[Tag].filter(_.id == lift(id)).delete)
  }
}
