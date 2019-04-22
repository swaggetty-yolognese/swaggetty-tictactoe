package game

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import game.LobbyActor._
import game.domain.{Player1, Player2, Room}
import scala.concurrent.duration._
import scala.collection.mutable

/**
  *
  * @param openRooms rooms with 1 player waiting for an opponent to join, publicly available.
  * @param closedRooms rooms with 2 players playing, available only to them.
  */
class LobbyActor(openRooms: mutable.Set[Room], closedRooms: mutable.Set[Room]) extends Actor with ActorLogging {

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
      log.debug(s"broadcasting update open={} closed={}", openRooms.size, closedRooms.size)
      context.system.eventStream.publish(makeLobbyUpdate())

    // create a room with one player
    case CreateRoom(p1) =>
      val roomId = UUID.randomUUID()
      val room = Room(roomId.toString, player1 = Some(Player1(p1)))
      openRooms.add(room)
      log.info(s"Creating a new room roomId=$roomId")
    //      val roomFsm = context.actorOf(RoomActor.props(roomId))
    //      roomFsm ! START_GAME(PLAYER_SIDE_1, PLAYER_SIDE_2)

    case JoinRoom(roomId, player) =>
      val room = openRooms.find(_.roomId == roomId).getOrElse {
        throw new IllegalArgumentException(s"Room not found! roomId=$roomId")
      }

      val room1 = room.copy(player2 = Some(Player2(player)))

      closedRooms.add(room1)

      // remove the existing open room
      openRooms.remove(room)
  }

  def makeLobbyUpdate() = LobbyUpdate(
    date = LocalDateTime.now(),
    rooms = openRooms.toSet
  )
}

object LobbyActor {

  def props() = Props(new LobbyActor(mutable.Set.empty[Room], mutable.Set.empty[Room]))

  sealed trait LobbyMsg
  case class LobbyUpdate(date: LocalDateTime, rooms: Set[Room]) extends LobbyMsg
  case class CreateRoom(player1: String) extends LobbyMsg
  case class JoinRoom(roomId: String, player: String) extends LobbyMsg
  case class EchoWs(msg: String) extends LobbyMsg
  case object BROADCAST_LOBBY_STATE extends LobbyMsg

}
