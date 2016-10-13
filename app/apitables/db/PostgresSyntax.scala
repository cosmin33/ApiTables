package apitables.db

import java.sql.SQLException

import apitables.Table
import apitables.exceptions._
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.libs.json.JsValue

/**
 * Created by cosmo on 27/12/14.
 */
object PostgresSyntax extends SqlSyntax {

  val ukViolationCode = "23505"
  val fkViolationCode = "23503"
  val ckViolationCode = "23514"
  val notNullViolationCode = "23502"
  val customViolationCode = "20100"

  private[this] val logger = Logger("apitables.Table")

  private[this] def getBaseSelect(table: Table) = {
    val sb: StringBuilder = new StringBuilder("select ")
    sb ++= (table.selectableFieldStrings ++ table.lookupRelationFieldStrings).mkString(", ")
    sb ++= "\nfrom " ++= table.name
    if (table.joinClause.isDefined)
      sb ++= "\n" ++= table.joinClause.get
    if (table.lookupJoins.isDefined)
      sb ++= "\n" ++= table.lookupJoins.get
    sb.toString()
  }

  override def getRowSql(table: Table): String = {
    val sb: StringBuilder = new StringBuilder(getBaseSelect(table))
    sb ++= "\nwhere " ++= table.fieldsPk.map(_.tableName + " = ?").mkString(" and ")
    if (table.whereClause.isDefined)
      sb ++= " and " ++= table.whereClause.get
    sb.toString()
  }

  override def getFilterSql(table: Table, params: Map[String, Any]): String = {
    def longParamToString(value: Any) = {
      value match {
        case jsValue: JsValue => jsValue.as[Long].toString
        case s: String => s
        case Some(s: String) => s
        case _ => throw new ApiTableException("Conversion error")
      }
    }
    val sb: StringBuilder = new StringBuilder(getBaseSelect(table))
    val paramsNotFound = params.keySet -- table.paramNames - Table.filterPageString - Table.filterPerPageString
    if (paramsNotFound.nonEmpty)
      throw new ApiTableFilterException(s"Parameters (${paramsNotFound.mkString(", ")}) do not correspond to filterable field parameters. Cannot filter")
    val selParamOptList = table.fieldsFilterable.map(field => field.getCondition(params))
    val selParamList = selParamOptList.flatten
    if (selParamList.nonEmpty) {
      sb ++= "\nwhere "
      if (table.whereClause.isDefined)
        sb ++= "(" ++= selParamList.mkString(" and ") ++= ") and " ++= table.whereClause.get
      else
        sb ++= selParamList.mkString(" and ")
    } else {
      if (table.whereClause.isDefined)
        sb ++= "\nwhere " ++= table.whereClause.get
    }
    if (table.orderByClause.isDefined)
      sb ++= "\norder by " ++= table.orderByClause.get
    val pageOpt = params.get(Table.filterPageString).map(longParamToString)
    val perPageOpt = params.get(Table.filterPerPageString).map(longParamToString)
    // process paging parameters (if present)
    if (pageOpt.isDefined) {
      val page = pageOpt.get.toLong
      val perPage = perPageOpt.map(_.toLong).getOrElse(Table.filterDefaultPerPage)
      val offset = (page - 1) * perPage
      sb ++= "\nlimit " ++= perPage.toString
      sb ++= "\noffset " ++= offset.toString
    }
    sb.toString()
  }

  override def getJoinSql(table: Table, params: Set[String]): String = {
    val sb: StringBuilder = new StringBuilder(getBaseSelect(table))
    val selParamOptList = params map { param =>
      val fieldOpt = table.fieldsFilterableMap.get(param)
      val field = fieldOpt.getOrElse {
        throw new ApiTableFilterException(s"Field '$param' from table '${table.name}' is not filterable, cannot use it in relations")
      }
      field.getExactCondition(params)
    }
    val selParamList = selParamOptList.flatten
    if (selParamList.nonEmpty) {
      sb ++= "\nwhere "
      if (table.whereClause.isDefined)
        sb ++= "(" ++= selParamList.mkString(" and ") ++= ") and " ++= table.whereClause.get
      else
        sb ++= selParamList.mkString(" and ")
    } else {
      if (table.whereClause.isDefined)
        sb ++= "\nwhere " ++= table.whereClause.get
    }
    if (table.orderByClause.isDefined)
      sb ++= "\norder by " ++= table.orderByClause.get
    sb.toString()
  }

  override def getInsertSql(table: Table): String = {
    val sb: StringBuilder = new StringBuilder("insert into ") ++= table.strippedName ++= "("
    sb ++= table.fieldsModifiable.map(_.strippedTableName).mkString(", ")
    sb ++= ") values ("
    val p = for (i <- 1 to table.fieldsModifiable.size) yield "?"
    sb ++= p.mkString(", ")
    sb ++= ")"
    if (table.fieldsInsertReturning.nonEmpty)
      sb ++= "\nreturning " ++= table.fieldsInsertReturning.map(_.strippedTableName).mkString(", ")
    sb.toString()
  }

  override def getUpdateSql(table: Table): String = {
    val sb: StringBuilder = new StringBuilder("update ") ++= table.strippedName ++= " set "
    sb ++= table.fieldsModifiable.map(_.strippedTableName + " = ?").mkString(", ")
    sb ++= "\nwhere "
    sb ++= table.fieldsPk.map(_.strippedTableName + " = ?").mkString(" and ")
    if (table.fieldsUpdateReturning.nonEmpty)
      sb ++= "\nreturning " ++= table.fieldsUpdateReturning.map(_.strippedTableName).mkString(", ")
    sb.toString()
  }

  override def getDeleteSql(table: Table): String = {
    val sb: StringBuilder = new StringBuilder("delete from ") ++= table.strippedName
    sb ++= "\nwhere "
    sb ++= table.fieldsPk.map(_.strippedTableName + " = ?").mkString(" and ")
    sb.toString()
  }

  override def transformException(exception: SQLException): Exception = exception match {
    case ex: PSQLException =>
      val msg = ex.getServerErrorMessage.getMessage
      if (ex.getSQLState == ukViolationCode) {
        """constraint "(\w*)"""".r.findFirstMatchIn(msg) match {
          case Some(m) =>
            val constraint = m.group(1)
            if (constraint != null)
              new ApiTableUKViolationException(constraint)
            else ex
          case None => ex
        }
      } else if (ex.getSQLState == fkViolationCode) {
        """foreign key constraint "(\w*)"""".r.findFirstMatchIn(msg) match {
          case Some(m) =>
            val constraint = m.group(1)
            if (constraint != null)
              new ApiTableFKViolationException(constraint)
            else ex
          case None => ex
        }
      } else if (ex.getSQLState == ckViolationCode) {
        """check constraint "(\w*)"""".r.findFirstMatchIn(msg) match {
          case Some(m) =>
            val constraint = m.group(1)
            if (constraint != null)
              new ApiTableCKViolationException(constraint)
            else ex
          case None => ex
        }
      } else if (ex.getSQLState == customViolationCode) {
        throw new ApiTableCustomViolationException(ex.getServerErrorMessage.getDetail, ex.getServerErrorMessage.getHint)
      } else {
        ex
      }
    case _ => exception
  }

}
