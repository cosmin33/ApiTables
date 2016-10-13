package controllers

import apitables.TableBuilder
import apitables.fields.{IntegerFB, StringFB, TimestampFB}

/**
 * Created by cosmo on 04/12/14.
 */
object ContactPersonController extends BaseController {
  val contactPersonBuilder = new TableBuilder("contact_person").fields(
    IntegerFB("id").tableName("id").primaryKey().build,
    TimestampFB("created").filterable().tableName("created").build,
    TimestampFB("updated").filterable().tableName("updated").build,
    IntegerFB("version").filterable().tableName("version").nullable().build,
    StringFB("contactPlan").filterable().tableName("contact_plan").nullable().build,
    StringFB("email").filterable().tableName("email").nullable().build,
    StringFB("name").filterable().tableName("name").build,
    IntegerFB("ownerId").filterable().tableName("owner_id").build,
    StringFB("ownerType").filterable().tableName("owner_type").build,
    StringFB("phone").filterable().tableName("phone").build,
    StringFB("role").filterable().tableName("role").build
  )

  val contactPersonTable = contactPersonBuilder.build

  def contactPersons = contactPersonTable.actionFilter()

}
