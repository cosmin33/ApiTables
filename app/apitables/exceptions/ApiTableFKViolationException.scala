package apitables.exceptions

/**
 * Created by cosmo on 05/01/15.
 */
class ApiTableFKViolationException(ex: RuntimeException, fkName: String) extends RuntimeException(ex) {
  def this(fkName: String) =
    this(new RuntimeException(s"Foreign key violation ($fkName)"), fkName)
}
