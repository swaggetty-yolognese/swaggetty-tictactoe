package game

import java.util.UUID

import akka.actor.FSM.Failure
import akka.actor.{FSM, Props}
import com.typesafe.scalalogging.LazyLogging
import game.RoomActor._
import game.domain._

// formatter: off
sealed trait RoomState
case object WAITING_FOR_INIT extends RoomState
case object WAITING_FOR_MOVE extends RoomState

sealed trait RoomEvent
case class START_GAME(player1: Player, player2: Player) extends RoomEvent // used to start a new game
case class MOVE(player: Player, coordinate: BoardCoordinate) extends RoomEvent
case class BROADCAST_ROOM_STATE() extends RoomEvent

sealed trait Data
case object Empty extends Data
case class GameData(roomId: UUID, board: Board, player1: Player, player2: Player, turn: Player, winner: Option[Player]) extends Data
// formatter: on

class RoomActor(roomId: UUID) extends FSM[RoomState, Data] with LazyLogging {

  startWith(WAITING_FOR_INIT, Empty)

  when(WAITING_FOR_INIT)({
    case Event(s: START_GAME, Empty) =>
      logger.info(s"starting a new game roomId=$roomId")
      val board = Board.emptyTicTacToeBoard()
      val startingPlayer = s.player1
      goto(WAITING_FOR_MOVE) using GameData(roomId, board, s.player1, s.player2, startingPlayer, winner = None)
    case Event(b: BROADCAST_ROOM_STATE, g: GameData) =>
      context.system.eventStream.publish(RoomUpdate(g.player1, g.player2, g.board, g.turn))
      stay
  })

  when(WAITING_FOR_MOVE)({
    case Event(b: BROADCAST_ROOM_STATE, g: GameData) =>
      context.system.eventStream.publish(RoomUpdate(g.player1, g.player2, g.board, g.turn))
      stay()
    case Event(move: MOVE, game @ GameData(roomId, board, _, _, turn, None)) =>
      if (move.player != turn)
        throw IllegalGameMove(s"received move for player${move.player} but turn=$turn !")

      logger.info(s"Adding move=$move to game=$game")

      //update board
      if (board.at(move.coordinate) != 0)
        throw IllegalBoardMove(s"already busy on coordinates=${move.coordinate}")

      val board1 = board.set(move.coordinate, move.player.marker)

      // if the player did TIC-TAC-TOE end the game and goto GAME_HALT
      if (hasTicTacToe(board1, move.player)) {
        logger.info(s"TIC-TAC-TOE!!")
        stop using game.copy(board = board1, winner = Some(move.player))
      }

      // check if draw!

      //send info back to the players
      //sender() ! board1
      stay using game.copy(board = board1, turn = opponentForMove(move, game))
  })

  whenUnhandled({
    case e: Event => stop(Failure(s"unhandled event=$e"))
  })

}

object RoomActor {

  case class RoomUpdate(player1: Player, player2: Player, board: Board, turn: Player)

  def opponentForMove(move: MOVE, gameData: GameData) = {
      move.player.side match {
        case PLAYER_SIDE_1 => gameData.player2
        case PLAYER_SIDE_2 => gameData.player1
      }
  }


  def props(roomId: UUID) = Props(new RoomActor(roomId))

  val winningCombos = List(
    ((0, 0), (0, 1), (0, 2)), // 123
    ((1, 0), (1, 1), (1, 2)), // 456
    ((2, 0), (2, 1), (2, 2)), // 789

    ((0, 0), (1, 0), (2, 0)), // 147
    ((0, 1), (1, 1), (2, 1)), // 258
    ((0, 2), (1, 2), (2, 2)), // 369

    ((0, 0), (1, 1), (2, 2)), // 159
    ((0, 2), (1, 1), (2, 0)) // 357
  )

  def hasTicTacToe(board: Board, player: Player): Boolean = winningCombos.exists {
    case ((x1, y1), (x2, y2), (x3, y3)) =>
      board.at(x1, y1) == player.marker &&
        board.at(x2, y2) == player.marker &&
        board.at(x3, y3) == player.marker
  }

}