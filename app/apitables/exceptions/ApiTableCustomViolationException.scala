package apitables.exceptions

/**
 * Created by cosmo on 05/01/15.
 */
class ApiTableCustomViolationException(ex: RuntimeException, detail: String, hint: String) extends RuntimeException(ex) {
  def this(detail: String, hint: String) =
    this(new RuntimeException(s"Custom violation (detail: $detail, hint: $hint)"), detail, hint)
}
