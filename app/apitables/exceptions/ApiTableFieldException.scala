package apitables.exceptions

/**
 * Created by cosmo on 01/12/14.
 */

class ApiTableFieldException(ex: RuntimeException) extends RuntimeException(ex) {
  def this(message:String) = this(new RuntimeException(message))
  def this(message:String, throwable: Throwable) = this(new RuntimeException(message, throwable))
}
