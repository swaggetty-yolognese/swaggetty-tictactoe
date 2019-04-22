package api

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.typesafe.scalalogging.LazyLogging
import game.LobbyActor.{CreateRoom, EchoWs, LobbyUpdate}
import game.RoomActor.RoomUpdate
import util.JsonSupport

import scala.util.Try

trait RoomApi extends JsonSupport with LazyLogging {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer

  implicit val lobbyActor: ActorRef

  val SESSION_COOKIE = "session-cookie"

  val roomRoute = (get & withSessionCookie(SESSION_COOKIE)) { sessionId =>
    path("rooms" / JavaUUID) { roomId =>
      handleWebSocketMessages(roomSocket)
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

  lazy val roomSocket: Flow[Message, TextMessage.Strict, NotUsed] = {

    val (flowInput, flowOutput) = Source.queue[String](10, OverflowStrategy.dropTail)
      .toMat(BroadcastHub.sink[String])(Keep.both)
      .run()

    // register an actor that feeds the queue when a lobbyActor update is received
    val socketActor = system.actorOf(Props(new Actor {
      override def preStart: Unit = {
        context.system.eventStream.subscribe(self, classOf[RoomUpdate])
      }

      def receive: Receive = {
        // INTERNAL -> SOCKET
        case u:RoomUpdate => flowInput.offer(json.write(u))

        // SOCKET -> INTERNAL
        case TextMessage.Strict(msg) =>
          logger.info(s"got $msg")
      }
    }))

    Flow[Message]
      .mapConcat { m => socketActor ! m; Nil } // send and forget to the actor the incoming data
      .merge(flowOutput) // Stream the data we want to the client
      .map(TextMessage.apply)

  }


}
