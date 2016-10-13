package controllers

import apitables.TableBuilder
import apitables.fields.{BooleanFB, IntegerFB, StringFB, TimestampFB}

/**
 * Created by cosmo on 11/12/14.
 */
object EventController extends BaseController {
  val eventBuilder = new TableBuilder("event e").fields(
    IntegerFB("id").tableName("e.id").primaryKey().build,
    TimestampFB("created").tableName("e.created").build,
    BooleanFB("acknowledged").tableName("e.acknowledged").nullable().build,
    TimestampFB("acknowledgedTime").tableName("e.acknowledged_time").nullable().build,
    TimestampFB("eventTime").tableName("e.event_time").build,
    StringFB("eventType").tableName("e.event_type").build,
    IntegerFB("owner.id").tableName("e.owner_id").build,
    StringFB("owner.type").tableName("e.owner_type").build,
    IntegerFB("relatedEvent").tableName("e.related_event").nullable().build,
    StringFB("sourceSystem").tableName("e.source_system").build,
    BooleanFB("requiresAck").tableName("e.requires_ack").build,
    IntegerFB("severityLevel").tableName("e.severity_level").build,
    StringFB("owner.name").tableName("coalesce(v.name, a.name, d.name)").lookup().build
  ).whereClause("e.id < 500")
  .joinClause(
    """|left join vpp v on e.owner_id = v.id and e.owner_type = 'vpp'
      |left join asset a on e.owner_id = a.id and e.owner_type = 'asset'
      |left join dispatch_group d on e.owner_id = d.id and e.owner_type = 'dispatch_group'""".stripMargin)

  val eventTable = eventBuilder.build

  def events = eventTable.actionFilter()

}
