package controllers

import apitables.fields._
import apitables.{RelationBuilder, Table, TableBuilder}

/**
 * Created by cosmo on 25/11/14.
 */
object AssetController extends BaseController {

  val assetsBuilder = new TableBuilder("asset a").fields(
    IntegerFB("id").tableName("a.id").primaryKey().filterable().insertReturning().build,
    TimestampFB("created").tableName("a.created").lookup().insertReturning().build,
    TimestampFB("updated").tableName("a.updated").lookup().updateReturning().build,
    IntegerFB("version").tableName("a.version").nullable().build,
    StringFB("address").tableName("a.address").build,
    StringFB("assetType").tableName("a.asset_type").build,
    StringFB("balancingGroup").tableName("a.balancing_group").nullable().build,
    StringFB("eegPlantNumber").tableName("a.eeg_plant_number").nullable().build,
    StringFB("eegPlantType").tableName("a.eeg_plant_type").nullable().build,
    DoubleFB("installedPower").tableName("a.installed_power").nullable().build,
    DoubleFB("latitude").tableName("a.latitude").nullable().build,
    DoubleFB("longitude").tableName("a.longitude").nullable().build,
    StringFB("meterPointNumber").tableName("a.meter_point_number").build,
    StringFB("name").tableName("a.name").build,
    IntegerFB("contractId").tableName("a.contract_id").nullable().build,
    IntegerFB("customer.id").tableName("a.customer_id").lookup().build,
    StringFB("assetState").tableName("a.asset_state").nullable().build,
    DoubleFB("priceFixed").tableName("a.price_fixed").nullable().build,
    StringFB("priceFormula").tableName("a.price_formula").nullable().build,
    DoubleFB("pricePenalty").tableName("a.price_penalty").nullable().build,
    StringFB("dsos").tableName("a.dsos").nullable().build,
    DoubleFB("toleranceMax").tableName("a.tolerance_max").nullable().build,
    DoubleFB("toleranceMin").tableName("a.tolerance_min").nullable().build,
    StringFB("steeringType").tableName("a.steering_type").nullable().build,
    DoubleFB("currentSetPoint").tableName("a.current_set_point").nullable().build,
    TimestampFB("currentSetPointCreatedAt").tableName("a.current_set_point_created_at").nullable().build,
    IntegerFB("currentVppId").tableName("a.current_vpp_id").nullable().build,
    DoubleFB("shutdownRampRate").tableName("a.shutdown_ramp_rate").nullable().build,
    DoubleFB("startupRampRate").tableName("a.startup_ramp_rate").nullable().build,
    StringFB("collectingBalancingGroup").tableName("a.collecting_balancing_group").build,
    StringFB("producerBalancingGroup").tableName("a.producer_balancing_group").nullable().build,
    StringFB("operationalBalancingGroup").tableName("a.operational_balancing_group").build,
    StringFB("supplierBalancingGroup").tableName("a.supplier_balancing_group").build,
    StringFB("profileIdMrlPlus").tableName("a.profile_id_mrl_plus").nullable().build,
    StringFB("profileIdMrlMinus").tableName("a.profile_id_mrl_minus").nullable().build,
    StringFB("checkOutBalancingGroupMinus").tableName("a.check_out_balancing_group_minus").nullable().build,
    StringFB("checkOutBalancingGroupPlus").tableName("a.check_out_balancing_group_plus").nullable().build,
    IntegerFB("tsoId").tableName("a.tso_id").nullable().build,
    DoubleFB("availabilityTolerance").tableName("a.availability_tolerance").nullable().build,
    StringFB("edmProfileNumber").tableName("a.edm_profile_number").nullable().build,
    StringFB("addressZipCode").tableName("a.address_zip_code").build,
    BooleanFB("producer").tableName("a.producer").build,
    BooleanFB("consumer").tableName("a.consumer").build,
    StringFB("checkOutBalancingGroup").tableName("a.check_out_balancing_group").nullable().build,
    // tso lookups
    TimestampFB("tso.created").tableName("t.created").lookup().build,
    TimestampFB("tso.updated").tableName("t.updated").lookup().build,
    IntegerFB("tso.version").tableName("t.version").lookup().build,
    StringFB("tso.countryCode").tableName("t.country_code").lookup().build,
    StringFB("tso.name").tableName("t.name").lookup().build,
    StringFB("tso.timeZone").tableName("t.time_zone").lookup().build,
    StringFB("tso.tsoId").tableName("t.tso_id").lookup().build
  ).joinClause("left join tso t on a.tso_id = t.id")
  val assetParametersBuilder = new TableBuilder("asset_parameters").fields(
    IntegerFB("id").filterable().primaryKey().insertReturning().build,
    TimestampFB("created").insertReturning().build,
    TimestampFB("updated").updateReturning().build,
    IntegerFB("version").tableName("version").nullable().build,
    IntegerFB("activationsPerDay").tableName("activations_per_day").nullable().build,
    IntegerFB("maxActivationTime").tableName("max_activation_time").nullable().build,
    IntegerFB("maxShutdownTime").tableName("max_shutdown_time").nullable().build,
    IntegerFB("maxUtilisationTime").tableName("max_utilisation_time").nullable().build,
    IntegerFB("minActivationTime").tableName("min_activation_time").nullable().build,
    IntegerFB("minShutdownTime").tableName("min_shutdown_time").nullable().build,
    IntegerFB("recoveryTime").tableName("recovery_time").nullable().build,
    IntegerFB("assetId").tableName("asset_id").filterable().build,
    IntegerFB("productId").tableName("product_id").build,
    DoubleFB("marketablePower").tableName("marketable_power").nullable().build,
    DoubleFB("minEnergyPrice").tableName("min_energy_price").build,
    DoubleFB("prequalifiedFlex").tableName("prequalified_flex").nullable().build
  )
  val assetDispatchGroupBuilder = new TableBuilder("asset_dispatch_group a").fields(
    IntegerFB("assetId").tableName("a.asset_id").primaryKey().filterable().build,
    IntegerFB("dispatchgroupsId").tableName("a.dispatchgroups_id").primaryKey().filterable().build,
    // dispatch group lookup
    IntegerFB("id").tableName("d.id").lookup().build,
    TimestampFB("created").tableName("d.created").lookup().build,
    TimestampFB("updated").tableName("d.updated").lookup().build,
    IntegerFB("version").tableName("d.version").lookup().build,
    StringFB("name").tableName("d.name").lookup().build,
    IntegerFB("marketId").tableName("d.market_id").lookup().build,
    IntegerFB("tsoId").tableName("d.tso_id").lookup().build,
    StringFB("dispatchMode").tableName("d.dispatch_mode").lookup().build,
    // market lookup
    IntegerFB("market.id").tableName("m.id").lookup().build,
    TimestampFB("market.created").tableName("m.created").lookup().build,
    StringFB("market.name").tableName("m.name").lookup().build,
    IntegerFB("market.intervalLength").tableName("m.interval_length").lookup().build,
    StringFB("market.intervalType").tableName("m.interval_type").lookup().build,
    IntegerFB("market.reactionTimeSeconds").tableName("m.reaction_time_seconds").lookup().build,
    // tso lookup
    IntegerFB("tso.id").tableName("t.id").lookup().build,
    TimestampFB("tso.created").tableName("t.created").lookup().build,
    TimestampFB("tso.updated").tableName("t.updated").lookup().build,
    IntegerFB("tso.version").tableName("t.version").lookup().build,
    StringFB("tso.countryCode").tableName("t.country_code").lookup().build,
    StringFB("tso.name").tableName("t.name").lookup().build,
    StringFB("tso.timeZone").tableName("t.time_zone").lookup().build,
    StringFB("tso.tsoId").tableName("t.tso_id").lookup().build
  ).joinClause("join dispatch_group d on a.dispatchgroups_id = d.id" +
    "\nleft join market m on d.market_id = m.id" +
    "\nleft join tso t on d.tso_id = t.id")
  val productBuilder = new TableBuilder("asset_product a").fields(
    IntegerFB("assetId").tableName("a.asset_id").primaryKey().filterable().build,
    IntegerFB("productsId").tableName("a.products_id").primaryKey().filterable().build,
    IntegerFB("id").tableName("p.id").build,
    TimestampFB("created").tableName("p.created").build,
    TimestampFB("updated").tableName("p.updated").build,
    IntegerFB("version").tableName("p.version").nullable().build,
    StringFB("name").tableName("p.name").nullable().build,
    StringFB("code").tableName("p.code").nullable().build,
    StringFB("marketType").tableName("p.market_type").nullable().build,
    IntegerFB("marketId").tableName("p.market_id").filterable().nullable().build,
    BooleanFB("htProduct").tableName("p.ht_product").nullable().build,
    BooleanFB("ntProduct").tableName("p.nt_product").nullable().build,
      // market lookup
    IntegerFB("market.id").tableName("m.id").lookup().build,
    TimestampFB("market.created").tableName("m.created").lookup().build,
    StringFB("market.name").tableName("m.name").lookup().build,
    IntegerFB("market.intervalLength").tableName("m.interval_length").lookup().build,
    StringFB("market.intervalType").tableName("m.interval_type").lookup().build,
    IntegerFB("market.reactionTimeSeconds").tableName("m.reaction_time_seconds").lookup().build
  ).joinClause("join product p on a.products_id = p.id" +
    "\nleft join market m on p.market_id = m.id")

  val assetsTable: Table = assetsBuilder.build
  val assetParametersTable = assetParametersBuilder.build
  val assetParamsRelation = new RelationBuilder("assetParameterSets")
    .master(assetsTable)
    .detail(assetParametersTable)
    .joinConditions(("id", "assetId"))
    .build
  val assetDispatchGroupTable = assetDispatchGroupBuilder.build
  val assetDispatchGroupRelation = new RelationBuilder("dispatchGroups")
    .master(assetsTable)
    .detail(assetDispatchGroupTable)
    .joinConditions(("id", "assetId"))
    .build
  val productTable = productBuilder.build
  val assetProductRelation = new RelationBuilder("products")
    .master(assetsTable)
    .detail(productTable)
    .joinConditions(("id", "assetId"))
    .build

//  productTable.addSelectListener { fields =>
//    // move market down the tree as market value
//    val market = new RowValue(
//      ("id", fields.remove("market_id").get),
//      ("created", fields.remove("market_created").get),
//      ("name", fields.remove("market_name").get),
//      ("intervalLength", fields.remove("market_intervalLength").get),
//      ("intervalType", fields.remove("market_intervalType").get),
//      ("reactionTimeSeconds", fields.remove("market_reactionTimeSeconds").get)
//    )
//    fields += ("market" -> market)
//  }

  override def mainTable: Table = assetsTable
}
