package org.stairwaybook.concurrentSimulation

import scala.actors._
import scala.actors.Actor._

case class WorkItem(time:Int, msg:Any, target:Actor)
case class AfterDelay(delay:Int, msg:Any, target:Actor)
case object Start
case object Stop
case class Ping(time:Int)
case class Pong(time:Int, from:Actor)

class Clock  extends Actor {
  private var running = false
  private var currentTime = 0
  private var agenda:List[WorkItem] = List()
  private var allSimulants:List[Actor] = List()
  private var busySimulants:Set[Actor] = Set()

  def add(sim:Simulant) = {
    allSimulants = sim :: allSimulants
  }

  override def act(): Unit = {
    loop {
      if (running && busySimulants.isEmpty) advance

      reactToOneMessage()
    }
  }

  def advance(): Unit = {
    if (agenda.isEmpty && currentTime > 0 ) {
      println(s"** Agenda empty. Clock exiting at time $currentTime.")
      self ! Stop
      return ()
    }

    currentTime += 1
    println(s"Advancing to time $currentTime")
    processCurrentEvents()
    for(sim<-allSimulants)
      sim ! Ping(currentTime)

    busySimulants  = Set.empty ++ allSimulants
  }

  private def processCurrentEvents(): Unit = {
    val todoNow = agenda.takeWhile(_.time <= currentTime)

    agenda = agenda.drop(todoNow.length)

    for (WorkItem(time, msg, target)<-todoNow) {
      assert(time == currentTime)
      target ! msg
    }
  }

  def reactToOneMessage(): Unit = {
    react {
      case AfterDelay(delay, msg, target) =>
        val item = WorkItem(currentTime + delay, msg, target)
        agenda = insert(agenda, item)

      case Pong(time, sim) =>
        assert(time==currentTime)
        assert(busySimulants contains sim)
        busySimulants -= sim

      case Start => running = true
      case Stop =>
        for (sim <- allSimulants)
          sim ! Stop
        exit()
    }
  }

  private def insert(ag:List[WorkItem], item:WorkItem):List[WorkItem] = {
    if(ag.isEmpty || item.time < ag.head.time) item :: ag
    else ag.head :: insert(ag.tail, item)
  }

}

trait Simulant extends Actor {
  val clock : Clock
  def handleSimMessage(msg:Any)
  def simStarting() {}

  override def act(): Unit = {
    loop {
      react {
        case Stop => exit()
        case Ping(time) =>
          if (time == 1 ) simStarting()
          clock ! Pong(time, self)
        case msg => handleSimMessage(msg)
      }
    }
  }

  start()
}

class Circuit{
  val clock = new Clock()
  // simulation messages
  case class SetSignal(sig:Boolean)
  case class SignalChanged(wire:Wire, sig:Boolean)

  // delay constants
  val WireDelay = 1
  val InverterDelay = 2
  val OrGateDelay = 3
  val AndGateDelay = 3
  // Wire and Gate classes and methods

  class Wire(name:String, init:Boolean) extends Simulant {
    def this(name:String) {
      this(name, false)
    }

    def this() { this("unnamed")}

    val clock = Circuit.this.clock
    clock.add(this)

    private var sigVal = init
    private var observers:List[Actor] = List()

    def handleSimMessage(msg:Any): Unit = msg match {
      case SetSignal(s) =>
        if (s!=sigVal) {
          sigVal = s
          signalObservers()
        }
    }

    def signalObservers(): Unit = {
      for (obs <- observers)
        clock ! AfterDelay(WireDelay, SignalChanged(this, sigVal), obs)
    }

    override def simStarting() = {
      signalObservers()
    }

    def addObserver(obs:Actor) = {
      observers = obs :: observers
    }

    override def toString = s"Wire($name)"
  }

  private object DummyWire extends Wire("dummy")

  abstract class Gate(in1:Wire, in2:Wire, out:Wire) extends Simulant {
    def computeOutput(s1:Boolean, s2:Boolean):Boolean
    val delay:Int
    val clock = Circuit.this.clock
    clock.add(this)
    in1.addObserver(this)
    in2.addObserver(this)
    var s1, s2 = false

    override def handleSimMessage(msg: Any): Unit = msg match {
      case SignalChanged(w, sig) =>
        if(w == in1) s1 = sig
        if (w == in2) s2 = sig
        clock ! AfterDelay(delay, SetSignal(computeOutput(s1, s2)), out)

    }
  }

  def orGate(in1: Wire, in2:Wire, output:Wire) = {
    new Gate(in1, in2, output) {
      val delay = OrGateDelay

      override def computeOutput(s1: Boolean, s2: Boolean): Boolean = s1 || s2
    }
  }

  def andGate(in1:Wire, in2:Wire, output:Wire) = {
    new Gate(in2, in2, output) {
      override def computeOutput(s1: Boolean, s2: Boolean): Boolean = s1 && s2

      override val delay: Int = AndGateDelay
    }
  }

  def inverter(in1:Wire, output:Wire) = {
    new Gate(in1, DummyWire, output) {
      override def computeOutput(s1: Boolean, s2: Boolean): Boolean = !s1

      override val delay: Int = InverterDelay
    }
  }

  def probe(wire:Wire) = new Simulant {
    override val clock: Clock = Circuit.this.clock
    clock.add(this)
    wire.addObserver(this)
    override def handleSimMessage(msg: Any): Unit = msg match {
      case SignalChanged(w, s) => println(s"signal $w changed to $s")
    }
  }

  def start() { clock ! Start}
}



