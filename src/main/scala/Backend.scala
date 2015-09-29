import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

object Backend {

  def props(host: String, port: Int) = Props(new Backend(host, port))
}


class Backend(host: String, port: Int) extends Actor with ActorLogging {


  case object Started


  val topicConnection = context.system.actorOf(TopicConnection.props("some-topic"))

  val cluster = Cluster(context.system)


  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {

    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }


  override def postStop(): Unit = {

    log.info("Backend at {}:{} stopping", host, port)
    cluster.unsubscribe(self)
  }


  def receive = {

    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      topicConnection ! TopicConnection.Message("some message")
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }
}
