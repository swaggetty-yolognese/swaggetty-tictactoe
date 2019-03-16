package game

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import game.LobbyActor._
import game.domain.{Player1, Room}

import scala.concurrent.duration._
import scala.collection.mutable

class LobbyActor(rooms: mutable.Set[Room]) extends Actor with ActorLogging {

  implicit val ec = context.system.dispatcher

  context.system.scheduler.schedule(
    initialDelay = 2 seconds,
    interval = 10 seconds,
    receiver = self,
    message = BROADCAST_LOBBY_STATE
  )

  override def receive: Receive = {

    case EchoWs(msg) => context.system.eventStream.publish(EchoWs(s"LobbyEcho: $msg"))

    // used to tell all the clients of the current state of the lobbyActor
    case BROADCAST_LOBBY_STATE =>
      log.debug(s"broadcasting update")
      context.system.eventStream.publish(makeLobbyUpdate())

    // create a room with one player
    case CreateRoom(p1) =>
      val roomId = UUID.randomUUID()
      val room = Room(roomId.toString, player1 = Some(Player1(p1)))
      rooms.add(room)
      log.info(s"Creating a new room roomId=$roomId")
      log.info("Not actually starting the room FSM")
    //      val roomFsm = context.actorOf(RoomActor.props(roomId))
    //      roomFsm ! START_GAME(PLAYER1, PLAYER2)
  }

  def makeLobbyUpdate() = LobbyUpdate(
    date = LocalDateTime.now(),
    rooms = rooms.toSet
  )
}

object LobbyActor {

  def props() = Props(new LobbyActor(mutable.Set.empty[Room]))

  sealed trait LobbyMsg
  case class CreateRoom(player1: String)
  case class LobbyUpdate(date: LocalDateTime, rooms: Set[Room])
  case class EchoWs(msg: String)
  case object BROADCAST_LOBBY_STATE extends LobbyMsg

}
