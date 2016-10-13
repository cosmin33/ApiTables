package apitables.fields

import java.sql.{PreparedStatement, ResultSet}

import apitables.exceptions.ApiTableException
import play.api.libs.json._

/**
 * Created by cosmo on 26/11/14.
 */
class BooleanField(builder: FieldBuilder) extends Field(builder) {

  private[this] def getParamValue(params: Map[String, Any]): Option[Boolean] = {
    getParamRawValue(name, params) match {
      case None => None
      case Some(p) =>
        p match {
          case v: Option[Any] => v map {
            case s: String => s.toBoolean
            case _ => throw new ApiTableException("Conversion error")
          }
          case v: BooleanValue => v.value
          case v: JsValue => Some(v.as[Boolean])
          case v: String => Some(v.toBoolean)
          case _ => throw new ApiTableException("Conversion error")
        }
    }
  }

  override private[apitables] def getCondition(params: Map[String, Any], forSelect: Boolean = true): Option[String] =
    getParamValue(params).map(_ => tableName + " = ?")

  override private[apitables] def setParam(stmt: PreparedStatement, index: Int, params: Map[String, Any], forSelect: Boolean = true, mandatory: Boolean = false): Int = {
    val paramValue = getParamValue(params)
    if (paramValue.isDefined) {
      stmt.setBoolean(index, paramValue.get)
      index + 1
    } else {
      if (mandatory) throwMandatory
      index
    }
  }

  private[apitables] def setParamNull(stmt: PreparedStatement, index: Int): Unit = {
    stmt.setNull(index, java.sql.Types.BOOLEAN)
  }

  override private[apitables] def getValue(theName: String, resultSet: ResultSet): FieldValue = {
    val s = resultSet.getBoolean(theName)
    if (resultSet.wasNull())
      new BooleanValue(None)
    else
      new BooleanValue(Some(s))
  }

  override private[apitables] def getValue(theName: String, params: Map[String, Any]): FieldValue =
    BooleanValue(getParamValue(params))
}

class BooleanFB(name: String) extends FieldBuilder(name) {

  override def build: Field = new BooleanField(this)
}

object BooleanFB {
  def apply(name: String): BooleanFB = new BooleanFB(name)
}
