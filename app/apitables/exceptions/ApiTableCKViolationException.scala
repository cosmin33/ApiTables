package apitables.exceptions

/**
 * Created by cosmo on 05/01/15.
 */
class ApiTableCKViolationException(ex: RuntimeException, ckName: String) extends RuntimeException(ex) {
  def this(ckName: String) =
    this(new RuntimeException(s"Unique key violation ($ckName)"), ckName)
}
