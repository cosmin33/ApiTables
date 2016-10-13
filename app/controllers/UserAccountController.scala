package controllers

import apitables._
import apitables.fields._

object UserAccountController extends BaseController {

  val userAccountBuilder = new TableBuilder("user_account").fields(
    IntegerFB("id").tableName("id").primaryKey().build,
    TimestampFB("created").tableName("created").build,
    TimestampFB("updated").tableName("updated").build,
    IntegerFB("version").tableName("version").nullable().build,
    StringFB("email").tableName("email").nullable().build,
    StringFB("name").tableName("name").nullable().build,
    StringFB("phone").tableName("phone").nullable().build,
    StringFB("password").tableName("password").nullable().build,
    StringFB("cryptPassword").tableName("crypt_password").nullable().build,
    StringFB("cryptSalt").tableName("crypt_salt").nullable().build
  )

  val userAccountTable = userAccountBuilder.build

  override def mainTable: Table = userAccountTable

}
