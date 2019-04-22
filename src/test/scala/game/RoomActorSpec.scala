package game

import game.domain._
import game.RoomActor._
import org.specs2.mutable.Specification

class RoomActorSpec extends Specification {
  
  val p1 = Player1("jj")
  val p2 = Player2("kk")
  
  "RoomActor" should {

    "add moves to the board" in {

      val emptyBoard = Board.emptyTicTacToeBoard()

      val zeroOne = BoardCoordinate(0, 1)

      emptyBoard.at(0, 1) === 0
      emptyBoard.set(zeroOne, p1).at(0, 1) === 1
      emptyBoard.set(zeroOne, p1).at(zeroOne) === 1
      emptyBoard.at(0, 1) === 0
      emptyBoard.set(zeroOne, p2).at(0, 1) === -1
      emptyBoard.set(zeroOne, p2).at(0, 0) === 0
    }

    "check if a player did tic-tac-toe" in {

      val emptyBoard = Board.emptyTicTacToeBoard()

      val boardWithTris = emptyBoard
        .set(BoardCoordinate(0, 0), p1.marker)
        .set(BoardCoordinate(0, 1), p1.marker)
        .set(BoardCoordinate(0, 2), p1.marker)

      hasTicTacToe(boardWithTris, p1) === true
      hasTicTacToe(emptyBoard, p1) === false
      hasTicTacToe(boardWithTris, p2) === false
      hasTicTacToe(boardWithTris, p2) === false

    }

  }

}