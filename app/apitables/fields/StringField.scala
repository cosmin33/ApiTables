package apitables.fields

import java.sql.{PreparedStatement, ResultSet}

import apitables.exceptions.ApiTableException
import play.api.libs.json.{JsNull, JsValue}

/**
 * Created by cosmo on 26/11/14.
 */
class StringField(builder: FieldBuilder) extends Field(builder) {

  val length = builder.asInstanceOf[StringFB]._length

  private[this] def getParamValue(params: Map[String, Any]): Option[String] = {
    getParamRawValue(name, params) match {
      case None => None
      case Some(p) =>
        p match {
          case v: Option[Any] => v map {
            case s: String => s
            case _ => throw new ApiTableException("Conversion error")
          }
          case v: StringValue => v.value
          case v: JsValue => if (v == JsNull) None else Some(v.as[String])
          case v: String => Some(v)
          case _ => throw new ApiTableException("Conversion error")
        }
    }
  }

  override private[apitables] def getCondition(params: Map[String, Any], forSelect: Boolean = true): Option[String] =
    getParamValue(params).map(_ => tableName + " like ?")

  override private[apitables] def setParam(stmt: PreparedStatement, index: Int, params: Map[String, Any], forSelect: Boolean = true, mandatory: Boolean = false): Int = {
    val paramValue = getParamValue(params)
    if (paramValue.isDefined) {
      stmt.setString(index, paramValue.get)
      index + 1
    } else {
      if (mandatory) throwMandatory
      index
    }
  }

  private[apitables] def setParamNull(stmt: PreparedStatement, index: Int): Unit = {
    stmt.setNull(index, java.sql.Types.VARCHAR)
  }

  override private[apitables] def getValue(theName: String, resultSet: ResultSet): FieldValue = {
    val s = resultSet.getString(theName)
    if (resultSet.wasNull())
      new StringValue(None)
    else
      new StringValue(Some(s))
  }

  override private[apitables] def getValue(theName: String, params: Map[String, Any]): FieldValue =
    StringValue(getParamValue(params))
}

class StringFB(name: String) extends FieldBuilder(name) {

  private[apitables] var _length = Int.MaxValue

  override def length(value: Int): FieldBuilder = {
    this._length = value
    this
  }

  override def build: Field = new StringField(this)
}

object StringFB {
  def apply(name: String): StringFB = new StringFB(name)
}
