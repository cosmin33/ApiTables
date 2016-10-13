package apitables.exceptions

/**
 * Created by cosmo on 17/12/14.
 */
class ApiTableJsonException(ex: RuntimeException) extends ApiTableException(ex) {
  def this(message:String) = this(new RuntimeException(message))
  def this(message:String, throwable: Throwable) = this(new RuntimeException(message, throwable))
}
