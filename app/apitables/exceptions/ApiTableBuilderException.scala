package apitables.exceptions

/**
 * Created by cosmo on 30/11/14.
 */
class ApiTableBuilderException(ex: RuntimeException) extends ApiTableException(ex) {
  def this(message:String) = this(new RuntimeException(message))
  def this(message:String, throwable: Throwable) = this(new RuntimeException(message, throwable))
}
