package example

import example.domain._
import example.RoomActor._
import org.specs2.mutable.Specification

class RoomActorSpec extends Specification {

  "RoomActor" should {

    "add moves to the board" in {

      val emptyBoard = Board.emptyTicTacToeBoard()

      val zeroOne = BoardCoordinate(0, 1)

      emptyBoard.at(0, 1) === 0
      emptyBoard.set(zeroOne, PLAYER1).at(0, 1) === 1
      emptyBoard.set(zeroOne, PLAYER1).at(zeroOne) === 1
      emptyBoard.at(0, 1) === 0
      emptyBoard.set(zeroOne, PLAYER2).at(0, 1) === -1
      emptyBoard.set(zeroOne, PLAYER2).at(0, 0) === 0
    }

    "check if a player did tic-tac-toe" in {

      val emptyBoard = Board.emptyTicTacToeBoard()

      val boardWithTris = emptyBoard
        .set(BoardCoordinate(0, 0), PLAYER1.marker)
        .set(BoardCoordinate(0, 1), PLAYER1.marker)
        .set(BoardCoordinate(0, 2), PLAYER1.marker)

      hasTicTacToe(boardWithTris, PLAYER1) === true
      hasTicTacToe(emptyBoard, PLAYER1) === false
      hasTicTacToe(boardWithTris, PLAYER2) === false
      hasTicTacToe(boardWithTris, PLAYER2) === false

    }

  }

}