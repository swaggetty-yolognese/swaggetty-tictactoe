package example

import akka.actor.{ ActorSystem, SupervisorStrategy }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import akka.util.Timeout
import api.LobbyApi
import example.LobbyActor.AddRoom
import util.SimpleSupervisor
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }

object AppEntryPoint extends App with LobbyApi with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("defaultActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  implicit val timeout = Timeout(5 seconds)

  lazy val config = com.typesafe.config.ConfigFactory.load()

  val lobbyActor = system.actorOf(SimpleSupervisor.props(
    LobbyActor.props(), "lobbyActor", SupervisorStrategy.Resume
  ))

  Http().bindAndHandle(lobbyRoute, "127.0.0.1", 8088).map { bind =>
    logger.info(s"bound to $bind")
  }

  lobbyActor ! AddRoom("Andrea", "Gianni")
  //
  //
  //  roomActor ! START_GAME(PLAYER1, PLAYER2)
  //  roomActor ! MOVE(PLAYER1, BoardCoordinate(0, 0))
  //  roomActor ! MOVE(PLAYER2, BoardCoordinate(2, 0))
  //  roomActor ! MOVE(PLAYER1, BoardCoordinate(0, 1))
  //  roomActor ! MOVE(PLAYER2, BoardCoordinate(2, 1))
  //  roomActor ! MOVE(PLAYER1, BoardCoordinate(0, 2))

}
