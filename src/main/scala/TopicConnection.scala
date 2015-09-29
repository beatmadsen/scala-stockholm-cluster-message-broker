import java.util.UUID

import akka.actor._
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}

object TopicConnection {


  /** message for incoming and outgoing messages to the client */
  case class Message(text: String)


  /** message to distribute across cluster */
  case class DistributedMessage(id: UUID, text: String)


  def props(topic: String) = Props(new TopicConnection(topic))
}


class TopicConnection(topic: String) extends Actor with ActorLogging {

  import TopicConnection._

  val id = UUID.randomUUID()

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(topic, self)


  def receive = {

    case Message(text) =>
      log.info("Got message from client {}: {}", id, text)
      mediator ! Publish(topic, DistributedMessage(id, text))


    case msg @ DistributedMessage(uuid, text) if uuid != id ⇒
      log.info("Got message from other cluster node with id {}: {}", id, msg)


    case Terminated(who)  ⇒
      // if we cannot send messages, we might just as well shut down business
      context.stop(self)

    case SubscribeAck(x) ⇒
      log.info("subscribing: {}", x);
  }


  override def postStop(): Unit = {

    log.info("Stopping client session {}", id.toString)
  }
}
