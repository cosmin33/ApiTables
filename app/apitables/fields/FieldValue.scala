package apitables.fields

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import play.api.libs.json._

import scala.collection.mutable


/**
 * Created by cosmo on 29/11/14.
 */
sealed trait FieldValue {
  def getJsonValue: JsValue
  def isNull: Boolean
}

case class IntegerValue(value: Option[Long]) extends FieldValue {
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) => JsNumber(v)
  }
  override def isNull: Boolean = value.isEmpty
}

case class BooleanValue(value: Option[Boolean]) extends FieldValue {
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) => JsBoolean(v)
  }
  override def isNull: Boolean = value.isEmpty
}

case class DoubleValue(value: Option[Double]) extends FieldValue {
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) => JsNumber(v)
  }
  override def isNull: Boolean = value.isEmpty
}

case class StringValue(value: Option[String]) extends FieldValue {
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) => JsString(v)
  }
  override def isNull: Boolean = value.isEmpty
}

case class DateValue(value: Option[Date]) extends FieldValue {
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) =>
      val fmt: DateTimeFormatter = ISODateTimeFormat.dateTime()
      val dt: DateTime = new DateTime(v)
      JsString(fmt.print(dt))
  }
  override def isNull: Boolean = value.isEmpty
}

case class TimestampValue(value: Option[Date]) extends FieldValue {
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) =>
      val fmt: DateTimeFormatter = ISODateTimeFormat.dateTime()
      val dt: DateTime = new DateTime(v)
      JsString(fmt.print(dt))
  }
  override def isNull: Boolean = value.isEmpty
}

case class RowValue(value: Option[List[(String, FieldValue)]]) extends FieldValue {
  def this(pair: (String, FieldValue)) = this(Some(List[(String, FieldValue)](pair)))
  def this(name: String, value: FieldValue) = this((name, value))
  def this(pairs: (String, FieldValue)*) = this(Some(pairs.toList))
  def this(list: List[(String, FieldValue)]) = this(if (list.isEmpty) None else Some(list))
  def this(map: mutable.Map[String, FieldValue]) = this(map.toList)
  def getJsonValue: JsValue = value match {
    case None => JsNull
    case Some(v) =>
      JsObject(v.map(r => (r._1, r._2.getJsonValue)).toList)
  }
  override def isNull: Boolean = value.isEmpty
}

case class DetailValue(value: List[RowValue]) extends FieldValue {
  override def getJsonValue: JsValue = JsArray(value.map(_.getJsonValue))
  override def isNull: Boolean = value.isEmpty
}
