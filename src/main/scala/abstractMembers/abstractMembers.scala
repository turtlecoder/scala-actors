package abstractMembers

trait Abstract {
  type T // an abstract type (T)
  def transform(x:T) : T // an abstract method
  val initial: T // An abstract immutable value
  var current: T // An abstract mutable value
}

class Concrete extends Abstract {
  override type T = String

  // an abstract type (T)
  override def transform(x: T): T = ???
  override val initial: T = ???
  override var current: T = ???
}
