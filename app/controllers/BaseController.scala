package controllers

import apitables.db.{PostgresSyntax, SqlSyntax}
import play.api.mvc.{Action, Controller}
import apitables.Table

/**
 * Created by cosmo on 27/12/14.
 */
class BaseController extends Controller {
  implicit val syntax: SqlSyntax = PostgresSyntax

  def mainTable: Table = null

  def filter = if (mainTable != null) mainTable.actionFilter() else Action(InternalServerError("no main table defined!"))
  def row = if (mainTable != null) mainTable.actionRow() else Action(InternalServerError("no main table defined!"))
  def insert = if (mainTable != null) mainTable.actionInsert() else Action(InternalServerError("no main table defined!"))
  def update = if (mainTable != null) mainTable.actionUpdate() else Action(InternalServerError("no main table defined!"))
  def delete = if (mainTable != null) mainTable.actionDelete() else Action(InternalServerError("no main table defined!"))
}
