package controllers

import apitables._
import apitables.fields._

object VppController extends BaseController {

  val vppBuilder = new TableBuilder("vpp v").fields(
    IntegerFB("id").tableName("v.id").primaryKey().build,
    TimestampFB("created").tableName("v.created").build,
    TimestampFB("updated").tableName("v.updated").build,
    IntegerFB("version").tableName("v.version").nullable().build,
    StringFB("name").tableName("v.name").build,
    DoubleFB("flexVolume").tableName("v.flex_volume").nullable().build,
    IntegerFB("productId").tableName("v.product_id").nullable().filterable().build,
    StringFB("vppStatus").tableName("v.vpp_status").nullable().build,
    TimestampFB("endTime").tableName("v.end_time").nullable().build,
    TimestampFB("startTime").tableName("v.start_time").nullable().build,
    DoubleFB("weightedAveragePrice").tableName("v.weighted_average_price").nullable().build,
    DoubleFB("currentSetPoint").tableName("v.current_set_point").nullable().build,
    DoubleFB("energyPrice").tableName("v.energy_price").nullable().build,
    TimestampFB("currentSetPointCreatedAt").tableName("v.current_set_point_created_at").nullable().build,
    IntegerFB("dispatchGroupId").tableName("v.dispatch_group_id").nullable().filterable().build,
    IntegerFB("tsoId").tableName("v.tso_id").nullable().build,
    BooleanFB("flexiblePricing").tableName("v.flexible_pricing").build
  )
  val dispatchGroupBuilder = new TableBuilder("dispatch_group dg").fields(
    IntegerFB("id").tableName("dg.id").primaryKey().build,
    TimestampFB("created").tableName("dg.created").build,
    TimestampFB("updated").tableName("dg.updated").build,
    IntegerFB("version").tableName("dg.version").nullable().build,
    StringFB("name").tableName("dg.name").build,
    IntegerFB("marketId").tableName("dg.market_id").nullable().filterable().build,
    IntegerFB("tsoId").tableName("dg.tso_id").nullable().filterable().build,
    StringFB("dispatchMode").tableName("dg.dispatch_mode").nullable().build
  )
  val tsoBuilder = new TableBuilder("tso t").fields(
    IntegerFB("id").tableName("t.id").primaryKey().build,
    TimestampFB("created").tableName("t.created").build,
    TimestampFB("updated").tableName("t.updated").build,
    IntegerFB("version").tableName("t.version").nullable().build,
    StringFB("countryCode").tableName("t.country_code").build,
    StringFB("name").tableName("t.name").build,
    StringFB("timeZone").tableName("t.time_zone").build,
    StringFB("tsoId").tableName("t.tso_id").nullable().build,
    StringFB("transmissionCode").tableName("t.transmission_code").nullable().build
  )
  val dgTsoBuilder = new TableBuilder("tso dgt").fields(
    IntegerFB("id").tableName("dgt.id").primaryKey().build,
    TimestampFB("created").tableName("dgt.created").build,
    TimestampFB("updated").tableName("dgt.updated").build,
    IntegerFB("version").tableName("dgt.version").nullable().build,
    StringFB("countryCode").tableName("dgt.country_code").build,
    StringFB("name").tableName("dgt.name").build,
    StringFB("timeZone").tableName("dgt.time_zone").build,
    StringFB("tsoId").tableName("dgt.tso_id").nullable().build,
    StringFB("transmissionCode").tableName("dgt.transmission_code").nullable().build
  )
  val dgMarketBuilder = new TableBuilder("market dgm").fields(
    IntegerFB("id").tableName("dgm.id").primaryKey().build,
    TimestampFB("created").tableName("dgm.created").build,
    StringFB("name").tableName("dgm.name").build,
    IntegerFB("intervalLength").tableName("dgm.interval_length").nullable().build,
    StringFB("intervalType").tableName("dgm.interval_type").nullable().build,
    IntegerFB("reactionTimeSeconds").tableName("dgm.reaction_time_seconds").nullable().build
  )
  val productBuilder = new TableBuilder("product p").fields(
    IntegerFB("id").tableName("p.id").primaryKey().build,
    TimestampFB("created").tableName("p.created").build,
    TimestampFB("updated").tableName("p.updated").build,
    IntegerFB("version").tableName("p.version").nullable().build,
    StringFB("name").tableName("p.name").nullable().build,
    StringFB("code").tableName("p.code").nullable().build,
    StringFB("marketType").tableName("p.market_type").nullable().build,
    IntegerFB("marketId").tableName("p.market_id").nullable().build,
    BooleanFB("htProduct").tableName("p.ht_product").nullable().build,
    BooleanFB("ntProduct").tableName("p.nt_product").nullable().build
  )
  val prodMarketBuilder = new TableBuilder("market pm").fields(
    IntegerFB("id").tableName("pm.id").primaryKey().build,
    TimestampFB("created").tableName("pm.created").build,
    StringFB("name").tableName("pm.name").build,
    IntegerFB("intervalLength").tableName("pm.interval_length").nullable().build,
    StringFB("intervalType").tableName("pm.interval_type").nullable().build,
    IntegerFB("reactionTimeSeconds").tableName("pm.reaction_time_seconds").nullable().build
  )

  val vppTable = vppBuilder.build
  val dispatchGroupTable = dispatchGroupBuilder.build
  val vppDgRelation = new RelationBuilder("dispatchGroup")
    .master(vppTable)
    .detail(dispatchGroupTable)
    .lookup()
    .joinConditions(("dispatchGroupId", "id"))
    .build
  val dgTsoTable = dgTsoBuilder.build
  val dispatchGroupTsoRelation = new RelationBuilder("tso")
    .master(dispatchGroupTable)
    .detail(dgTsoTable)
    .lookup()
    .joinConditions(("tsoId", "id"))
    .build
  val dgMarketTable = dgMarketBuilder.build
  val dispatchGroupMarketRelation = new RelationBuilder("market")
    .master(dispatchGroupTable)
    .detail(dgMarketTable)
    .lookup()
    .joinConditions(("marketId", "id"))
    .build
  val tsoTable = tsoBuilder.build
  val vppTsoRelation = new RelationBuilder("tso")
    .master(vppTable)
    .detail(tsoTable)
    .lookup()
    .joinConditions(("tsoId", "id"))
    .build
  val productTable = productBuilder.build
  val vppProductRelation = new RelationBuilder("product")
    .master(vppTable)
    .detail(productTable)
    .lookup()
    .joinConditions(("productId", "id"))
    .build
  val productMarketTable = prodMarketBuilder.build
  val productMarketRelation = new RelationBuilder("market")
    .master(productTable)
    .detail(productMarketTable)
    .lookup()
    .joinConditions(("marketId", "id"))
    .build

  override def mainTable: Table = vppTable

}
