import akka.actor._

object Backend {

  def props(host: String, port: Int) = Props(new Backend(host, port))
}


/**
 * Publishes http endpoints "/" and "/[topic]" and websocket endpoint "/[topic]/ws"
 */
class Backend(host: String, port: Int) extends Actor with ActorLogging {


  case object Started


  val topicConnection = context.system.actorOf(TopicConnection.props("some-topic"))

  self ! Started
  topicConnection ! TopicConnection.Message("some message")     // TODO: Only do this when a new node comes on.


  def receive = {

    case Started ⇒
      log.info("Backend started at {}:{}", host, port)
  }


  override def postStop(): Unit = {

    log.info("Backend at {}:{} stopping", host, port)
  }
}
