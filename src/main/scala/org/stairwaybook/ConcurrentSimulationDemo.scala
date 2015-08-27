package org.stairwaybook

import concurrentSimulation._

/**
 * Created by hkhanhex on 8/26/15.
 */
package object ConcurrentSimulationDemo {
  def main(args:Array[String] = Array() ): Unit ={
    val circuit = new Circuit with Adders
    import circuit._
    val ain = new Wire("ain", true)
    val bin = new Wire("bin", false)
    val cin = new Wire("cin", true)
    val sout = new Wire("sout")
    val cout = new Wire("cout")
    probe(ain)
    probe(bin)
    probe(cin)
    probe(sout)
    probe(cout)
    fullAdder(ain, bin, cin, sout, cout)
    circuit.start()
  }
}
