package apitables.exceptions

/**
 * Created by cosmo on 28/11/14.
 */
class ApiTableException(ex: RuntimeException) extends RuntimeException(ex) {
  def this(message:String) = this(new RuntimeException(message))
  def this(message:String, throwable: Throwable) = this(new RuntimeException(message, throwable))
}
