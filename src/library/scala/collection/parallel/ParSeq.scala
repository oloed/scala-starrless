package scala.collection.parallel



import scala.collection.generic.GenericCompanion
import scala.collection.generic.GenericParCompanion
import scala.collection.generic.GenericParTemplate
import scala.collection.generic.ParFactory
import scala.collection.generic.CanCombineFrom
import scala.collection.parallel.mutable.ParArrayCombiner
import scala.collection.parallel.mutable.ParArray


/** A template trait for parallel sequences.
 *  
 *  $parallelseqinfo
 *  
 *  $sideeffects
 */
trait ParSeq[+T] extends Seq[T]
                    with ParIterable[T]
                    with GenericParTemplate[T, ParSeq]
                    with ParSeqLike[T, ParSeq[T], Seq[T]] {
  override def companion: GenericCompanion[ParSeq] with GenericParCompanion[ParSeq] = ParSeq
  
  def apply(i: Int): T
  
  override def toString = super[ParIterable].toString
}


object ParSeq extends ParFactory[ParSeq] {
  implicit def canBuildFrom[T]: CanCombineFrom[Coll, T, ParSeq[T]] = new GenericCanCombineFrom[T]
  
  def newBuilder[T]: Combiner[T, ParSeq[T]] = ParArrayCombiner[T]
  
  def newCombiner[T]: Combiner[T, ParSeq[T]] = ParArrayCombiner[T]
}


























