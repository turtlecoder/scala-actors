package org.stairwaybook.concurrentSimulation

import scala.actors._
import scala.actors.Actor._

case class WorkItem(time:Int, msg:Any, target:Actor)
case class AfterDelay(delay:int, msg:Any, target:Actor)
case object Start
case object Stop

class Clock  extends Actor {
  private var running = false
  private var currentTime = 0
  private var agenda:List[WorkItem] = List()
  private var allSimulants:List[Actor] = List()
  private var busySimulants:Set[Actor] = Set.empty()

  private add(sim:Simulant) = {
    allSimulants = sim :: allSimulants
  }

  override def act(): Unit = {
    loop {
      if (running && busySimulants.isEmpty) advance

      reactToOneMessage()
    }
  }

  def advance() = {
    if (agenda.isEmpty && currenTime > 0 ) {
      println(s"** Agenda empty. Clock exiting at time $currentTime.")
      self ! stop
      return
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
    private observers:List[Actor] = List()
  }

  // msic. utility methods

}



