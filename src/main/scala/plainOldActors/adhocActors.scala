package plainOldActors

/**
 * Created by hkhanhex on 8/24/15.
 */
package object adhocActors {
  import scala.actors._
  import Actor._
  val seriousActor2 = actor {
    for(i <- 1 to 5){
      println("That is the question!")
      Thread.sleep(1000)
    }
  }

  val echoActor = actor {
    while(true){
      receive {
        case msg =>
          println(s"Recieved Message: $msg")
      }
    }
  }

  val intActor = actor {
    receive {
      case x:Int =>
        println(s"Got an Int: $x")
    }
  }

  class NameResolver extends Actor {
    import java.net.{InetAddress, UnknownHostException}
    def act(): Unit ={
      react{
        case (name:String, actor:Actor) =>
          actor ! getIp(name)
          act()

        case "EXIT" =>
          println("Name resolver exiting")

        case msg =>
          println(s"Unhandled Exception: $msg")
          act()
      }

    }

    def getIp(name:String):Option[InetAddress] = try {
      Some(InetAddress.getByName(name))
    } catch {
      case _:UnknownHostException => None
    }
  }

  object NameResolverOne extends NameResolver

  /*

scala> NameResolverTwo ! (("www.cnn.com", self))

scala> self.receiveWithin(0) { case x => x }
res5: Any = Some(www.cnn.com/199.27.79.73)

scala>
   */

  object NameResolverTwo extends NameResolver {
    override def act(): Unit = {
      loop{
        react {
          case (name:String, actor:Actor) =>
            actor ! getIp(name)

          case msg =>
            println(s"Unhandled Message: $msg")
        }
      }
    }
  }

  val anotherSillyActor = actor {
    def emoteLater() = {
      val mainActor = self
      actor {
        Thread.sleep(1000)
        mainActor ! "Emote"
      }
    }

    var emoted = 0

    emoteLater()

    loop {
      react {
        case "Emote" =>
          println("I'm acting!")
          emoted += 1
          if (emoted < 5)
            emoteLater()
        case msg =>
          println(s"Received: $msg")
      }
    }
  }

  import java.net.{InetAddress, UnknownHostException}

  case class LookupIp(name:String, respondTo:Actor)
  case class LookupResult(name:String, address:Option[InetAddress])

  object NameResolverThree extends NameResolver {
    override def act(): Unit = {
      loop{
        react {
          case LookupIp(name, actor) =>
            actor ! LookupResult(name, getIp(name))
        }
      }
    }
  }

}
