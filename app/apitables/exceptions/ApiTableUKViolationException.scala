package apitables.exceptions

/**
 * Created by cosmo on 05/01/15.
 */
class ApiTableUKViolationException(ex: RuntimeException, ukName: String) extends RuntimeException(ex) {
  def this(ukName: String) =
    this(new RuntimeException(s"Unique key violation ($ukName)"), ukName)
}
