package plainOldActors

import scala.actors.Actor

/**
 * Created by hkhanhex on 8/24/15.
 */
object SillyActor extends Actor {
  def act(): Unit ={
    for (i<- 1 to 5) {
      println("I'm acting!")
      Thread.sleep(1000)
    }
  }
}
