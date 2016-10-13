package apitables.db

import java.sql.SQLException

import apitables.Table

/**
 * Created by cosmo on 27/12/14.
 */
trait SqlSyntax {
  def getFilterSql(table: Table, params: Map[String, Any]): String
  def getJoinSql(table: Table, params: Set[String]): String
  def getRowSql(table: Table): String
  def getInsertSql(table: Table): String
  def getUpdateSql(table: Table): String
  def getDeleteSql(table: Table): String

  def transformException(exception: SQLException): Exception = exception
}
