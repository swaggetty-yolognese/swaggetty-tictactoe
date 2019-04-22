package game

package object domain {

  case class Room(
                   roomId: String,
                   player1: Option[Player] = None,
                   player2: Option[Player] = None
                 )

  case class BoardCoordinate(x: Int, y: Int) {
    require(0 <= x && x <= 3 && 0 <= y && y <= 3, s"Invalid coordinates for board must be between 0 and 3 include, x=$x y=$y")
  }

  case class Board(private val grid: List[List[Int]]) {
    require(grid.length == 3 && grid.last.length == 3, s"Invalid size for board grid.length=${grid.length}")

    def at(c: BoardCoordinate): Int = grid(c.x)(c.y)

    def at(x: Int, y: Int): Int = grid(x)(y)

    def set(c: BoardCoordinate, p: Player): Board = set(c, p.marker)

    def set(c: BoardCoordinate, el: Int): Board = {
      Board(grid.updated(c.x, grid(c.x).updated(c.y, el)))
    }

    override def toString: String = {
      s"\n${grid(0)(0)}|${grid(0)(1)}|${grid(0)(2)} " +
      s"\n${grid(1)(0)}|${grid(1)(1)}|${grid(1)(2)} " +
      s"\n${grid(2)(0)}|${grid(2)(1)}|${grid(2)(2)}\n"
    }
  }

  object Board {
    def emptyTicTacToeBoard() = {
      val grid = List(
        List(0, 0, 0),
        List(0, 0, 0),
        List(0, 0, 0)
      )
      Board(grid)
    }
  }

  sealed trait Player {

    def marker: Int
    def side: PlayerSide
    val name: String

  }

  sealed trait PlayerSide
  case object PLAYER_SIDE_1 extends PlayerSide
  case object PLAYER_SIDE_2 extends PlayerSide

  case class Player1(name: String) extends Player {
    override def marker = 1
    override def side: PlayerSide = PLAYER_SIDE_1
    override def toString: String = s"PLAYER_1=${name.take(20)}"
  }

  case class Player2(name: String) extends Player {
    override def marker = -1
    override def side: PlayerSide = PLAYER_SIDE_2
    override def toString: String = s"PLAYER_2=${name.take(20)}"
  }

  case class IllegalBoardMove(msg: String) extends RuntimeException(msg)
  case class IllegalGameMove(msg: String) extends RuntimeException(msg)
}

