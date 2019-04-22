package api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import game.LobbyActor.{CreateRoom, EchoWs, JoinRoom, LobbyUpdate}
import util.JsonSupport
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

trait LobbyApi extends JsonSupport with LazyLogging {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer

  implicit val lobbyActor: ActorRef

  lazy val lobbyRoute: Route = pathPrefix("lobby"){
    (get & path("ws")) {
      handleWebSocketMessages(lobbySocket)
    } ~
    (post & path("join")) {
      entity(as[JoinRoom]) { join =>
        complete(s"JOIN=$join")
      }
    }
  }

  lazy val lobbySocket: Flow[Message, TextMessage.Strict, NotUsed] = {

    val (flowInput, flowOutput) = Source.queue[String](10, OverflowStrategy.dropTail)
      .toMat(BroadcastHub.sink[String])(Keep.both)
      .run()

    // register an actor that feeds the queue when a lobbyActor update is received
    val socketActor = system.actorOf(Props(new Actor {
      override def preStart: Unit = {
        context.system.eventStream.subscribe(self, classOf[LobbyUpdate])
        context.system.eventStream.subscribe(self, classOf[EchoWs])
      }

      def receive: Receive = {
        // INTERNAL -> SOCKET
        case u @ LobbyUpdate(date, rooms) => flowInput.offer(json.write(u))
        case EchoWs(msg)                  => flowInput.offer(msg)

        // SOCKET -> INTERNAL
        case TextMessage.Strict(msg) =>
          Try(json.read[CreateRoom](msg)).foreach(ar => lobbyActor.tell(ar, self))
          Try(json.read[EchoWs](msg)).foreach(echo => lobbyActor.tell(echo, self))
      }
    }))

    Flow[Message]
      .mapConcat { m => socketActor ! m; Nil } // send and forget to the actor the incoming data
      .merge(flowOutput) // Stream the data we want to the client
      .map(TextMessage.apply)

  }

}