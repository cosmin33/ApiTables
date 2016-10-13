package controllers

import apitables.TableBuilder
import apitables.fields.{DoubleFB, IntegerFB, StringFB, TimestampFB}

/**
 * Created by cosmo on 14/12/14.
 */
object PriceController extends BaseController {
  val priceBuilder = new TableBuilder("price").fields(
    IntegerFB("id").tableName("id").primaryKey().build,
    TimestampFB("created").tableName("created").build,
    TimestampFB("updated").tableName("updated").build,
    IntegerFB("version").tableName("version").nullable().build,
    DoubleFB("energyContractualPriceNeg").tableName("energy_contractual_price_neg").nullable().build,
    DoubleFB("energyContractualPricePos").tableName("energy_contractual_price_pos").nullable().build,
    DoubleFB("energyFactor").tableName("energy_factor").build,
    DoubleFB("energyInternalPriceNeg").tableName("energy_internal_price_neg").nullable().build,
    DoubleFB("energyInternalPricePos").tableName("energy_internal_price_pos").nullable().build,
    StringFB("formula").tableName("formula").nullable().build,
    StringFB("priceType").tableName("price_type").build,
    DoubleFB("wapFactor").tableName("wap_factor").build,
    IntegerFB("assetId").tableName("asset_id").build,
    IntegerFB("contractId").tableName("contract_id").build,
    DoubleFB("minEnergyPrice").tableName("min_energy_price").nullable().build
  )

  val priceTable = priceBuilder.build

  def filterPrice = priceTable.actionFilter()
}
