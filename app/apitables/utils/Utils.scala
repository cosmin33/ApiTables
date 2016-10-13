package apitables.utils

/**
 * Created by cosmo on 30/11/14.
 */
object Utils {
  @annotation.tailrec
  def findFirstDuplicate[T](list: Traversable[T], seen: Set[T] = Set[T]()): Option[T] =
    list match {
      case x :: xs => if (seen.contains(x)) new Some(x) else findFirstDuplicate(xs, seen + x)
      case _ => None
    }

}
