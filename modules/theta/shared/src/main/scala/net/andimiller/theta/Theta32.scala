package net.andimiller.theta

import cats.implicits._
import cats.kernel.BoundedSemilattice

import scala.collection.immutable.SortedSet

/** Data structure which represents a sampling of hash values for a stream of values, which can be used to estimate the unique items seen
  * @param retainedHashes hashes we're keeping track of
  * @param theta the highest hash value we're keeping track of, moved to the range 0.0 to 1.0
  * @tparam LgK log 2 value of the number of retained items, static and known at compile time
  */
case class Theta32[LgK <: Int with Singleton: ValueOf](retainedHashes: SortedSet[Int] = SortedSet.empty, theta: Double = 1) {
  assume(valueOf[LgK] <= 30)

  def hashToTheta(hash: Int): Double =
    (hash.toDouble / Int.MaxValue) / 2 + 0.5d

  private lazy val entries: Int = 1 << valueOf[LgK]

  def inEstimationMode: Boolean = retainedHashes.size == entries
  def inExactMode: Boolean      = retainedHashes.size < entries

  def addHashes(hashes: IterableOnce[Int]): Theta32[LgK] = {
    val newHashes = (SortedSet.from(hashes.iterator.filter(i => hashToTheta(i) < theta)).take(entries) ++ retainedHashes).take(entries)
    val topHash   = newHashes.takeRight(1).headOption
    val newTheta  = topHash.map(hashToTheta).getOrElse(1d)
    Theta32(newHashes, newTheta)
  }

  def getEstimate: Double =
    if (inExactMode)
      retainedHashes.size.toDouble
    else
      ((retainedHashes.size - 1).toDouble / theta)

}

object Theta32 {
  def empty[LgK <: Int with Singleton: ValueOf]: Theta32[LgK] = Theta32[LgK]()

  def fromHashes[LgK <: Int with Singleton: ValueOf](hashes: IterableOnce[Int]): Theta32[LgK] =
    Theta32[LgK]().addHashes(hashes)

  /** `empty` and `addAll` form a lawful BoundedSemilattice
    */
  implicit def instances[LgK <: Int with Singleton: ValueOf]: BoundedSemilattice[Theta32[LgK]] = new BoundedSemilattice[Theta32[LgK]] {
    override def combine(x: Theta32[LgK], y: Theta32[LgK]): Theta32[LgK] =
      x.addHashes(y.retainedHashes)

    override def empty: Theta32[LgK] = Theta32[LgK]()
  }
}
