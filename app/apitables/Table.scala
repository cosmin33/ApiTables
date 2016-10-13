package apitables

import java.sql.{ResultSet, Connection, PreparedStatement, SQLException}

import apitables.db.SqlSyntax
import apitables.exceptions.{ApiTableBuilderException, ApiTableJsonException}
import apitables.fields.{Field, FieldValue, RowValue}
import apitables.utils.Utils
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.http.ContentTypes
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.mvc.Action
import play.api.mvc.BodyParsers.parse
import play.api.mvc.Results._

import scala.collection.mutable

class Table(private val builder: TableBuilder) {
  val name = builder.name
  val joinClause = builder._joinClause
  val whereClause = builder._whereClause
  val orderByClause = builder._orderByClause
  val fields = builder._fields
  val syntax = builder.syntax
  val (alias, strippedName) = """^((\w+) +)*(\w+)$""".r.findFirstMatchIn(name) match {
    case Some(m) =>
      if (m.group(2) != null)
        (Some(m.group(3)): Some[String], m.group(2))
      else
        (Option.empty[String], m.group(3))
    case None =>
      throw new ApiTableBuilderException(s"Cannot strip the table name from the alias. Invalid table name for '$name'")
  }

  private[apitables] var relations: Set[Relation] = Set()
  private[apitables] var parentRelation: Option[Relation] = Option.empty[Relation]
  private[apitables] lazy val relationsLookup = relations.filter(_.lookup)
  private[apitables] lazy val relationsNonLookup = relations.filter(!_.lookup)

  val paramNames = fields.map(_.getParamNames).flatten.toSet

  type SelectListener = mutable.LinkedHashMap[String, FieldValue] => Unit
  type ModifListener = mutable.LinkedHashMap[String, FieldValue] => Unit

  private[this] var selectListeners: Set[SelectListener] = Set()
  private[this] var insertListeners: Set[ModifListener] = Set()
  private[this] var updateListeners: Set[ModifListener] = Set()
  private[this] var deleteListeners: Set[ModifListener] = Set()

  private[apitables] val groups = {
    val g = fields.filter(_.hasGroup).groupBy(_.groupName)
    val duplicates = fields.filter(f => g.keySet.contains(f.name)).map("'" + _.name + "'")
    if (duplicates.nonEmpty)
      throw new ApiTableBuilderException(s"field name ${duplicates.mkString(", ")} same as group name. Cannot build")
    g
  }

  val fieldsSelectable = fields.filter(_.selectable)
  val fieldsSelectableNoGroup = fields.filter(f => f.selectable && !f.hasGroup)
  val fieldsVisible = fields.filter(_.visible)
  val fieldsVisibleNoGroup = fields.filter(f => f.visible && !f.hasGroup)
  val fieldsPk = fields.filter(_.primaryKey)
  val fieldsFilterable = fields.filter(_.filterable)
  val fieldsFilterableMap = fieldsFilterable.map(field => (field.name, field)).toMap
  val fieldsModifiable = fields.filter(!_.lookup)
  val fieldsUpdateReturning = fields.filter(_.updateReturning)
  val fieldsInsertReturning = fields.filter(_.insertReturning)

  private[this] val logger = Logger("apitables.Table")

  private[this] def logStatement(stmt: PreparedStatement) = {
    logger.debug(s"""
      |table = $name, SQL:
      |=================================================================================================
      |$stmt
      |=================================================================================================""".stripMargin
    )
  }

  private[apitables] lazy val lookupJoins: Option[String] = {
    Option {
      val relSet = relations.filter(_.lookup).map { relation =>
        "left join " + relation.detail.name + " on " +
          relation.joinConditions.map { join =>
            val leftField = relation.master.fields.find(_.name == join._1).get.tableName
            val rightField = relation.detail.fields.find(_.name == join._2).get.tableName
            leftField + " = " + rightField
          }.mkString(" and ") + relation.detail.lookupJoins.map("\n" + _).getOrElse("")
      }
      if (relSet.nonEmpty) relSet.mkString("\n") else null
    }
  }

  private[apitables] lazy val selectableFieldStrings: List[String] = {
    fieldsSelectable map { field =>
      if (field.name == field.tableName)
        field.name
      else {
        val n = if (field.hasGroup) "\"" + field.name + "\"" else field.name
        field.tableName + " as " + n
      }
    }
  }

  private[apitables] lazy val selectableLookupFieldStrings: List[String] = {
    var comment = "\n-- " + name + "\n"
    fieldsSelectable map { field =>
      val s = comment + field.tableName + " as \"" + alias.get + "." + field.name + "\""
      comment = ""
      s
    }
  }

  private[apitables] lazy val lookupRelationFieldStrings: List[String] = {
    relationsLookup.toList flatMap { r =>
      r.detail.selectableLookupFieldStrings ++
        r.detail.lookupRelationFieldStrings
    }
  }

  private[apitables] def prepareForFilter(conn: Connection, params: Set[String]) = {
    val result = conn.prepareStatement(syntax.getJoinSql(this, params))
    result.setFetchSize(50)
    result
  }

  private[apitables] def prepareDetailsForFilter(conn: Connection): Map[Relation, PreparedStatement] = {
    logger.debug(s"prepareDetailsForFilter: table = $name")
    if (relations.isEmpty)
      Map.empty[Relation, PreparedStatement]
    else {
      val details = relationsNonLookup.map(relation => (relation, relation.detail.prepareForFilter(conn, relation.joinConditions.values.toSet))).toMap
      val subDetailsSet = relationsNonLookup.map(relation => relation.detail.prepareDetailsForFilter(conn))
      val subDetails = subDetailsSet.foldLeft(Map.empty[Relation, PreparedStatement])(_ ++ _)
      val allDetails = details ++ subDetails
      allDetails
    }
  }

  private[apitables] def doGetRows(conn: Connection,
                                   params: Map[String, Any],
                                   statements: Map[Relation, PreparedStatement],
                                   stmt: PreparedStatement) = {
    var i = 1
    for (field <- fieldsFilterable)
      i = field.setParam(stmt, i, params, forSelect = true, mandatory = false)
    val rs = stmt.executeQuery()
    val jsObjectList = Iterator.continually((rs.next(), rs)).takeWhile(_._1) map { case (bool, resultSet) =>
      val row = fieldsVisibleNoGroup.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()){ (acc, field) =>
        acc += (field.name -> field.getValue(resultSet))
      }
      val groupValues = groups map { case (group, groupedFields) =>
        val groupRow = groupedFields.map(f => f.inGroupName -> f.getValue(resultSet))
        (group, new RowValue(groupRow))
      }
      row ++= groupValues
      for (listener <- selectListeners)
        listener(row)
      for (relation <- relationsNonLookup)
        relation.selectDetails(conn, statements, row)
      for (relation <- relationsLookup)
        relation.selectLookupDetails(rs, row)
      row
    }
    jsObjectList.toList.map(new RowValue(_))
  }

  def getRows(conn: Connection, params: Map[String, Any]) = {
    val stmt = conn.prepareStatement(syntax.getFilterSql(this, params))
    logStatement(stmt)
    doGetRows(conn, params, prepareDetailsForFilter(conn), stmt)
  }

  /**
   * Filters the table rows conforming to the parameters and returns the filtered rows in a JsArray
   * @param conn a jdbc connection
   * @param params a map containing the parameters; any parameter value from the map can be a JsValue, a String or a descendent of [[apitables.fields.FieldValue]]
   * @return
   */
  def getRowsJson(conn: Connection, params: Map[String, Any]) =
    JsArray(getRows(conn, params).map(_.getJsonValue))

  private[apitables] def doGetRow(conn: Connection,
                                  params: Map[String, Any],
                                  statements: Map[Relation, PreparedStatement],
                                  stmt: PreparedStatement) = {
    var i = 1
    for (field <- fieldsPk)
      i = field.setParam(stmt, i, params, forSelect = false, mandatory = true)
    val rs = stmt.executeQuery()
    if (rs.next()) {
      val row = fieldsVisibleNoGroup.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()) { (a, f) =>
        a += (f.name -> f.getValue(rs))
      }
      val groupValues = groups map { case (group, groupedFields) =>
        val groupRow = groupedFields.map(f => f.inGroupName -> f.getValue(rs))
        (group, new RowValue(groupRow))
      }
      row ++= groupValues
      for (listener <- selectListeners)
        listener(row)
      for (relation <- relationsNonLookup)
        relation.selectDetails(conn, statements, row)
      for (relation <- relationsLookup)
        relation.selectLookupDetails(rs, row)
      new RowValue(row)
    } else
      new RowValue(None)
  }

  def getRow(conn: Connection, params: Map[String, Any]) = {
    val stmt = conn.prepareStatement(syntax.getRowSql(this))
    logStatement(stmt)
    doGetRow(conn, params, prepareDetailsForFilter(conn), stmt)
  }

  private[apitables] def getLookupRow(resultSet: ResultSet) = {
    val row = fieldsVisibleNoGroup.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()) { (a, f) =>
      a += (f.name -> f.getValue(this.alias.get + "." + f.name, resultSet))
    }
    val groupValues = groups map { case (group, groupedFields) =>
      val groupRow = groupedFields.map(f => f.inGroupName -> f.getValue(resultSet))
      (group, new RowValue(groupRow))
    }
    row ++= groupValues
    for (relation <- relationsLookup)
      relation.selectLookupDetails(resultSet, row)
    new RowValue(row)
  }

  /**
   * Returns a row from the table filtered by it's primary key
   *
   * If the params parameters does not contain all the primary key fields then exception is retuned
   * @param conn a jdbc connection
   * @param params a map containing the parameters; any parameter value from the map can be a JsValue, a String or a descendent of [[apitables.fields.FieldValue]]
   * @return
   */
  def getRowJson(conn: Connection, params: Map[String, Any]) =
    getRow(conn, params).getJsonValue

  private[this] def doGetInsert(conn: Connection, params: Map[String, Any], stmt: PreparedStatement) = {
    logStatement(stmt)
    val row = fields.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()){ (acc, field) =>
      acc += (field.name -> field.getValue(params))
    }
    for (listener <- insertListeners)
      listener(row)
    var i = 1
    val rowMap = row.toMap
    for (field <- fieldsModifiable) {
      val newIndex = field.setParam(stmt, i, rowMap, forSelect = false, mandatory = !field.nullable)
      if (newIndex == i) {
        field.setParamNull(stmt, i)
      }
      i = i + 1
    }
    try {
      stmt.execute()
    }
    catch {
      case ex: SQLException =>
        throw syntax.transformException(ex)
    }
    val rs = Option(stmt.getResultSet)
    if (rs.isDefined && rs.get.next()) {
      val returningFields = fieldsInsertReturning.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()) { (acc, field) =>
        acc += (field.name -> field.getValue(rs.get))
      }
      new RowValue(returningFields)
    } else
      new RowValue(None)
  }

  def getInsert(conn: Connection, params: Map[String, Any]) = {
    doGetInsert(conn, params, conn.prepareStatement(syntax.getInsertSql(this)))
  }

  def getInsertJson(conn: Connection, params: Map[String, JsValue]) =
    getInsert(conn, params).getJsonValue

  def getInsertJson(conn: Connection, params: JsArray) = {
    val stmt = conn.prepareStatement(syntax.getInsertSql(this))
    val value = for (jsValue <- params.value) yield doGetInsert(conn, jsValue.as[JsObject].fieldSet.toMap, stmt)
    new JsArray(value.map(_.getJsonValue))
  }

  def doGetUpdate(conn: Connection, params: Map[String, Any], stmt: PreparedStatement) = {
    logStatement(stmt)
    val row = fields.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()) { (acc, field) =>
      acc += (field.name -> field.getValue(params))
    }
    for (listener <- updateListeners)
      listener(row)
    var i = 1
    val rowMap = row.toMap
    for (field <- fieldsModifiable) {
      val newIndex = field.setParam(stmt, i, rowMap, forSelect = false, mandatory = !field.nullable)
      if (newIndex == i) {
        field.setParamNull(stmt, i)
      }
      i = i + 1
    }
    for (field <- fieldsPk)
      i = field.setParam(stmt, i, rowMap, forSelect = false, mandatory = true)
    stmt.execute()
    val rs = Option(stmt.getResultSet)
    if (rs.isDefined && rs.get.next()) {
      val returningFields = fieldsUpdateReturning.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()) { (acc, field) =>
        acc += (field.name -> field.getValue(rs.get))
      }
      new RowValue(returningFields)
    } else
      new RowValue(None)
  }

  def getUpdate(conn: Connection, params: Map[String, Any]) =
    doGetUpdate(conn, params, conn.prepareStatement(syntax.getUpdateSql(this)))

  def getUpdateJson(conn: Connection, params: Map[String, JsValue]) =
    getUpdate(conn, params).getJsonValue

  def getUpdateJson(conn: Connection, params: JsArray) = {
    val stmt = conn.prepareStatement(syntax.getUpdateSql(this))
    val value = for (jsValue <- params.value) yield doGetUpdate(conn, jsValue.as[JsObject].fieldSet.toMap, stmt)
    new JsArray(value.map(_.getJsonValue))
  }

  def doGetDelete(conn: Connection, params: Map[String, Any], stmt: PreparedStatement) = {
    logStatement(stmt)
    val row = fields.foldLeft(new mutable.LinkedHashMap[String, FieldValue]()) { (acc, field) =>
      acc += (field.name -> field.getValue(params))
    }
    for (listener <- deleteListeners)
      listener(row)
    var i = 1
    val rowMap = row.toMap
    for (field <- fieldsPk)
      i = field.setParam(stmt, i, rowMap, forSelect = false, mandatory = true)
    stmt.executeUpdate()
    new RowValue(None)
  }

  def getDelete(conn: Connection, params: Map[String, Any]) = {
    doGetDelete(conn, params, conn.prepareStatement(syntax.getDeleteSql(this)))
  }

  def getDeleteJson(conn: Connection, params: Map[String, JsValue]) =
    getDelete(conn, params).getJsonValue

  def getDeleteJson(conn: Connection, params: JsArray) = {
    val stmt = conn.prepareStatement(syntax.getDeleteSql(this))
    val value = for (jsValue <- params.value) yield doGetDelete(conn, jsValue.as[JsObject].fieldSet.toMap, stmt)
    new JsArray(value.map(_.getJsonValue))
  }

  def addSelectListener(listener: SelectListener) = {
    selectListeners += listener
    this
  }

  def addInsertListener(listener: ModifListener) = {
    insertListeners += listener
    this
  }

  def addUpdateListener(listener: ModifListener) = {
    updateListeners += listener
    this
  }

  def addDeleteListener(listener: ModifListener) = {
    deleteListeners += listener
    this
  }

  private[apitables] def addRelation(relation: Relation) = {
    relations = relations + relation
    if (relation.lookup && alias.isEmpty)
      throw new ApiTableBuilderException(s"A table who is master for a lookup relation should have an alias (table name: $name)")
  }

  private[apitables] def setParentRelation(relation: Relation) = {
    def checkRelationsCycle(table: Table): Unit = {
      if (table == this)
        throw new ApiTableBuilderException(s"Relation cycle detected; cannot build! (detail table: $name)")
      if (table.parentRelation.isDefined)
        checkRelationsCycle(table.parentRelation.get.master)
    }
    if (parentRelation.isDefined)
      throw new ApiTableBuilderException(s"A table cannot be the detail of more than one relation. (detail table: $name)")
    parentRelation = Some(relation)
    checkRelationsCycle(parentRelation.get.master)
  }

  def actionFilter() = Action { implicit request =>
    DB.withConnection { conn =>
      val params = request.queryString.map(e => (e._1, e._2.headOption))
      val json = this.getRowsJson(conn, params)
      Ok(Json.prettyPrint(json)).as(ContentTypes.JSON)
    }
  }

  def actionRow() = Action { implicit request =>
    DB.withConnection { conn =>
      val params = request.queryString.map(e => (e._1, e._2.headOption))
      val json = this.getRowJson(conn, params)
      Ok(Json.prettyPrint(json)).as(ContentTypes.JSON)
    }
  }

  def actionInsert() = Action(parse.json) { implicit request =>
    DB.withConnection { conn =>
      val json = request.body match {
        case v: JsArray => getInsertJson(conn, v)
        case v: JsObject => getInsertJson(conn, v.fieldSet.toMap)
        case _ => throw new ApiTableJsonException("Json incorrect for insert: expected JsObject or JsArray, found otherwise")
      }
      Ok(Json.prettyPrint(json)).as(ContentTypes.JSON)
    }
  }

  def actionUpdate() = Action(parse.json) { implicit request =>
    DB.withConnection { conn =>
      val json = request.body match {
        case v: JsArray => getUpdateJson(conn, v)
        case v: JsObject => getUpdateJson(conn, v.fieldSet.toMap)
        case _ => throw new ApiTableJsonException("Json incorrect for update: expected JsObject or JsArray, found otherwise")
      }
      Ok(Json.prettyPrint(json)).as(ContentTypes.JSON)
    }
  }

  def actionDelete() = Action(parse.json) { implicit request =>
    DB.withConnection { conn =>
      val json = request.body match {
        case v: JsArray => getDeleteJson(conn, v)
        case v: JsObject => getDeleteJson(conn, v.fieldSet.toMap)
        case _ => throw new ApiTableJsonException("Json incorrect for delete: expected JsObject or JsArray, found otherwise")
      }
      Ok(Json.prettyPrint(json)).as(ContentTypes.JSON)
    }
  }

}

object Table {
  val filterPageString = "page"
  val filterPerPageString = "per_page"
  val filterDefaultPerPage: Long = 20
}

class TableBuilder(val name: String) {
  private[apitables] var _joinClause: Option[String] = None
  private[apitables] var _whereClause: Option[String] = None
  private[apitables] var _orderByClause: Option[String] = None
  private[apitables] var _fields: List[Field] = List()
  private[apitables] var syntax: SqlSyntax = null

  def joinClause(value: String) = {
    _joinClause = Some(value)
    this
  }

  def whereClause(value: String) = {
    _whereClause = Some(value)
    this
  }

  def orderByClause(value: String) = {
    _orderByClause = Some(value)
    this
  }

  def fields(value: Traversable[Field]): TableBuilder = {
    _fields = value.toList
    this
  }

  def fields(value: Field*): TableBuilder = {
    fields(value.toList)
    this
  }

  def build(implicit syntax: SqlSyntax): Table = {
    this.syntax = syntax
    val names = _fields.map(_.name)
    val duplicateName = Utils.findFirstDuplicate(names)
    if (duplicateName.isDefined)
      throw new ApiTableBuilderException(s"Table field property 'name' duplicate found: '${duplicateName.get}'. Cannot build table")
    new Table(this)
  }

}
