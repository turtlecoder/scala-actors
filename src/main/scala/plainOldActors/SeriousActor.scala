package plainOldActors

import scala.actors._

/**
 * Created by hkhanhex on 8/24/15.
 */
object SeriousActor extends Actor{
  def act(): Unit = {
    for (i <- 1 to 5){
      println("To be or not to be")
      Thread.sleep(1000)
    }
  }
}
