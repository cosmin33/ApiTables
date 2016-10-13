package apitables

import java.sql.{ResultSet, Connection, PreparedStatement}

import apitables.exceptions.{ApiTableBuilderException, ApiTableInternalException, ApiTableRelationException}
import apitables.fields.{DetailValue, FieldValue, RowValue}

import scala.collection.immutable.List
import scala.collection.mutable

/**
 * Created by cosmo on 30/11/14.
 */
class Relation(private val builder: RelationBuilder) {
  val name = builder.name
  val master: Table = builder._master
  val detail: Table = builder._detail
  val oneToOne: Boolean = builder._oneToOne
  val lookup: Boolean = builder._lookup

  val joinConditions: Map[String, String] = {
    for (pair <- builder._joinConditions) {
      if (master.fields.find(_.name == pair._1).isEmpty)
        throw new ApiTableBuilderException(s"Cannot find master field ${pair._1} from table ${master.name}. Cannot build")
      if (detail.fieldsFilterable.find(_.name == pair._2).isEmpty)
        throw new ApiTableBuilderException(s"Cannot find detail field ${pair._2}, or the field isn't filterable. Cannot build")
    }
    builder._joinConditions
  }

  master.addRelation(this)
  detail.setParentRelation(this)

  private[apitables] def selectLookupDetails(resultSet: ResultSet, fields: mutable.LinkedHashMap[String, FieldValue]): Unit = {
    val newValue = (name, detail.getLookupRow(resultSet))
    fields += newValue
  }

  private[apitables] def selectDetails(conn: Connection,
                                       statements: Map[Relation, PreparedStatement],
                                       fields: mutable.LinkedHashMap[String, FieldValue]): Unit =
  {
    val params = for ((mfName, dfName) <- joinConditions) yield {
      val fieldValue = fields.getOrElse(mfName, throw new ApiTableRelationException(s"Cannot find master field $mfName. Cannot establish relation"))
      (dfName, fieldValue)
    }
    val hasNullParams = params.exists(_._2.isNull)
    val stmt = statements.getOrElse(this,
      throw new ApiTableInternalException(s"Internal error: cannot find prepared statement for detail table '${detail.name} in relation"))
    val newField =
      if (oneToOne)
        (name, if (hasNullParams) new RowValue(None) else detail.doGetRow(conn, params, statements, stmt))
      else
        (name, if (hasNullParams) new DetailValue(List()) else new DetailValue(detail.doGetRows(conn, params, statements, stmt)))
    fields += newField
  }

}

class RelationBuilder(val name: String) {
  var _master: Table = null
  var _detail: Table = null
  var _oneToOne: Boolean = false
  var _lookup: Boolean = false
  var _joinConditions: Map[String, String] = Map()

  def master(value: Table): RelationBuilder = {
    _master = value
    this
  }

  def detail(value: Table): RelationBuilder = {
    _detail = value
    this
  }

  def oneToOne(value: Boolean = true): RelationBuilder = {
    _oneToOne = value
    this
  }

  def lookup(value: Boolean = true): RelationBuilder = {
    _lookup = value
    if(_lookup) _oneToOne = true
    this
  }

  def joinConditions(value: Map[String, String]): RelationBuilder = {
    _joinConditions = value
    this
  }

  def joinConditions(value: (String, String)*): RelationBuilder = {
    joinConditions(value.toMap)
    this
  }

  def build: Relation = {
    if (_master == null || _detail == null)
      throw new ApiTableBuilderException("master and detail must both be specified. Cannot build")
    new Relation(this)
  }
}