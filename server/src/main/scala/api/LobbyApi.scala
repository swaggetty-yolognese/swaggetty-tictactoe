package api

import java.util.UUID

import akka.NotUsed
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive1, Route }
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, Sink, Source }
import example.LobbyActor.{ AddRoom, EchoWs, LobbyUpdate }
import util.JsonSupport
import akka.pattern._
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

trait LobbyApi extends JsonSupport with LazyLogging {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer

  implicit val lobbyActor: ActorRef

  val SESSION_COOKIE = "session-cookie"

  lazy val lobbyRoute: Route = {
    (get & path("lobby" / "ws")) {
      handleWebSocketMessages(makeSocketHandler)
    }
  }

  def withSessionCookie(name: String): Directive1[String] = optionalCookie(name).tflatMap {
    case Tuple1(Some(cookiePair)) => provide(cookiePair.value)
    case Tuple1(None) =>
      val newSessionId = UUID.randomUUID().toString
      setCookie(HttpCookie(name, newSessionId)).tflatMap { _ =>
        provide(newSessionId)
      }
  }

  lazy val makeSocketHandler: Flow[Message, TextMessage.Strict, NotUsed] = {

    val (flowInput, flowOutput) = Source.queue[String](10, OverflowStrategy.dropTail).toMat(BroadcastHub.sink[String])(Keep.both).run()

    // register an actor that feeds the queue when a lobbyActor update is received
    val socketActor = system.actorOf(Props(new Actor {
      override def preStart: Unit = {
        context.system.eventStream.subscribe(self, classOf[LobbyUpdate])
        context.system.eventStream.subscribe(self, classOf[EchoWs])
      }

      def receive: Receive = {
        // INTERNAL -> SOCKET
        case u @ LobbyUpdate(date, rooms) => flowInput.offer(serialization.write(u))
        case EchoWs(msg)                  => flowInput.offer(msg)

        // SOCKET -> INTERNAL
        case TextMessage.Strict(msg) =>
          Try(serialization.read[AddRoom](msg)).foreach(ar => lobbyActor.tell(ar, self))
          Try(serialization.read[EchoWs](msg)).foreach(echo => lobbyActor.tell(echo, self))
      }
    }))

    Flow[Message]
      .mapConcat { m => socketActor ! m; Nil } // send and forget to the actor the incoming data
      .merge(flowOutput) // Stream the data we want to the client
      .map(TextMessage.apply)

  }

}