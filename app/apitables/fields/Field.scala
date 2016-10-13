package apitables.fields

import java.sql.{PreparedStatement, ResultSet}

import apitables.exceptions.{ApiTableBuilderException, ApiTableJsonException, ApiTableNullException}
import play.api.libs.json.JsObject

/**
 * Created by cosmo on 25/11/14.
 */
abstract class Field(builder: FieldBuilder) {
  val name = builder.name
  val tableName = builder._tableName
  val primaryKey = builder._primaryKey
  val nullable = builder._nullable
  val lookup = builder._lookup
  val selectable = builder._selectable
  val filterable = builder._filterable
  val visible = builder._visible
  val insertReturning = builder._insertReturning
  val updateReturning = builder._updateReturning
  val (hasGroup, groupName, inGroupName) = """^((\w+)\.)*(\w+)""".r.findFirstMatchIn(name) match {
    case Some(m) =>
      val gr = m.group(2) != null
      if (gr && !selectable)
        throw new ApiTableBuilderException(s"To create a group the field must be selectable. Else you should not use '.' in field name. Invalid field '$name'")
      (gr, m.group(2), m.group(3))
    case None =>
      throw new ApiTableBuilderException(s"Cannot strip the field name from the group name. Invalid field name for '$name'")
  }
  val strippedTableName = """^(\w+\.)*(\w+)""".r.findFirstMatchIn(tableName) match {
    case Some(m) =>
      m.group(2)
    case None =>
      throw new ApiTableBuilderException(s"Cannot strip the field name from the table alias. Invalid field tableName for '$name'")
  }

  private[apitables] def getExactCondition(params: Set[String]): Option[String] = {
    if (params.contains(name))
      Some(tableName + " = ?")
    else
      None
  }

  private[apitables] def throwMandatory = {
    throw new ApiTableNullException(name)
  }

  private[apitables] def getParamRawValue(name: String, params: Map[String, Any]) = {
    val po = params.get(name)
    if (po.isDefined)
      po
    else {
      val pg = params.get(groupName)
      if (pg.isEmpty)
        None
      else {
        pg match {
          case Some(pgo: JsObject) =>
            pgo.fieldSet.toMap.get(inGroupName)
          case _ =>
            throw new ApiTableJsonException(s"group parameter $groupName is not JsObject, cannot search parameters in it")
        }
      }
    }
  }

  private[apitables] def getParamNames = {List(name)}
  private[apitables] def getCondition(params: Map[String, Any], forSelect: Boolean = true): Option[String]
  private[apitables] def setParam(stmt: PreparedStatement,
                                  index: Int,
                                  params: Map[String, Any],
                                  forSelect: Boolean = true,
                                  mandatory: Boolean = false): Int
  private[apitables] def setParamNull(stmt: PreparedStatement, index: Int): Unit
  private[apitables] def getValue(resultSet: ResultSet): FieldValue = getValue(name, resultSet)
  private[apitables] def getValue(theName: String, resultSet: ResultSet): FieldValue
  private[apitables] def getValue(params: Map[String, Any]): FieldValue = getValue(name, params)
  private[apitables] def getValue(theName: String, params: Map[String, Any]): FieldValue
}

object Field {
  val suffixFrom = "_from"
  val suffixTo = "_to"
}

class FieldBuilder(val name: String) {
  private[apitables] var _tableName: String = name
  private[apitables] var _primaryKey: Boolean = false
  private[apitables] var _nullable: Boolean = false
  private[apitables] var _lookup: Boolean = false
  private[apitables] var _selectable: Boolean = true
  private[apitables] var _filterable: Boolean = false
  private[apitables] var _visible: Boolean = true
  private[apitables] var _insertReturning: Boolean = false
  private[apitables] var _updateReturning: Boolean = false

  def tableName(value: String): FieldBuilder = {
    this._tableName = value
    this
  }

  def primaryKey(value: Boolean = true): FieldBuilder = {
    this._primaryKey = value
    if (value) this._filterable = true
    this
  }

  def nullable(value: Boolean = true): FieldBuilder = {
    this._nullable = value
    this
  }

  def lookup(value: Boolean = true): FieldBuilder = {
    if (value && _primaryKey)
      throw new ApiTableBuilderException(s"A primary key field cannot be lookup (for field '$name')")
    this._lookup = value
    if (value) this._nullable = true
    this
  }

  def filterable(value: Boolean = true): FieldBuilder = {
    if (!value && _primaryKey)
      throw new ApiTableBuilderException(s"A primary key field must be filterable (for field '$name')")
    this._filterable = value
    this
  }

  def invisible(): FieldBuilder = {
    this._visible = false
    this
  }

  def updateReturning(value: Boolean = true): FieldBuilder = {
    this._updateReturning = value
    this
  }

  def selectable(value: Boolean = true): FieldBuilder = {
    this._selectable = value
    this._visible = value
    this
  }

  def insertReturning(value: Boolean = true): FieldBuilder = {
    this._insertReturning = value
    this
  }

  def length(value: Int): FieldBuilder = {
    throw new ApiTableBuilderException("length is not a valid property of this field class")
  }

  def build: Field = {
    throw new ApiTableBuilderException("build must be called from a subclass of FieldBuilder")
  }

}