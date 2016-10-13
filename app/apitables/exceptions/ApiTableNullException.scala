package apitables.exceptions

/**
 * Created by cosmo on 05/01/15.
 */
class ApiTableNullException(ex: RuntimeException, fieldName: String) extends ApiTableException(ex) {
  def this(fieldName: String) = this(new RuntimeException(s"Cannot find mandatory parameter '$fieldName'"), fieldName)
}
