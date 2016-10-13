package apitables.fields

import java.sql.{PreparedStatement, ResultSet}

import apitables.exceptions.ApiTableException
import play.api.libs.json.JsValue

/**
 * Created by cosmo on 26/11/14.
 */
class DoubleField(builder: FieldBuilder) extends Field(builder) {

  private[this] val nameFrom = name + Field.suffixFrom
  private[this] val nameTo = name + Field.suffixTo

  override private[apitables] def getParamNames: List[String] = List(name, nameFrom, nameTo)

  private[this] def getParamValue(name: String, params: Map[String, Any]): Option[Double] = {
    getParamRawValue(name, params) match {
      case None => None
      case Some(p) =>
        p match {
          case v: Option[Any] => v map {
            case s: String => s.toDouble
            case _ => throw new ApiTableException("Conversion error")
          }
          case v: DoubleValue => v.value
          case v: JsValue => Some(v.as[Double])
          case v: String => Some(v.toDouble)
          case _ => throw new ApiTableException("Conversion error")
        }
    }
  }

  override private[apitables] def getCondition(params: Map[String, Any], forSelect: Boolean = true): Option[String] = {
    val exactCondition = getParamValue(name, params).map(_ => tableName + " = ?")
    if (exactCondition.isDefined)
      exactCondition
    else {
      if (forSelect) {
        val from = getParamValue(nameFrom, params).map(_ => tableName + " >= ?")
        val to = getParamValue(nameTo, params).map(_ => tableName + " <= ?")
        val l = List(from, to).flatten
        if (l.isEmpty) None
        else Some(l.mkString(" and "))
      } else {
        None
      }
    }
  }

  override private[apitables] def setParam(stmt: PreparedStatement, index: Int, params: Map[String, Any], forSelect: Boolean = true, mandatory: Boolean = false): Int = {
    var i = index
    def setOneParam(oneName: String) = {
      val paramValue = getParamValue(oneName, params)
      if (paramValue.isDefined) {
        stmt.setDouble(i, paramValue.get)
        i = i + 1
      }
    }
    setOneParam(name)
    if (forSelect) {
      setOneParam(nameFrom)
      setOneParam(nameTo)
    }
    if (i == index && mandatory) throwMandatory
    i
  }

  private[apitables] def setParamNull(stmt: PreparedStatement, index: Int): Unit = {
    stmt.setNull(index, java.sql.Types.DOUBLE)
  }

  override private[apitables] def getValue(theName: String, resultSet: ResultSet): FieldValue = {
    val s = resultSet.getDouble(theName)
    if (resultSet.wasNull())
      new DoubleValue(None)
    else
      new DoubleValue(Some(s))
  }

  override private[apitables] def getValue(theName: String, params: Map[String, Any]): FieldValue =
    DoubleValue(getParamValue(theName, params))
}

class DoubleFB(name: String) extends FieldBuilder(name) {

  override def build: Field = new DoubleField(this)
}

object DoubleFB {
  def apply(name: String): DoubleFB = new DoubleFB(name)
}
