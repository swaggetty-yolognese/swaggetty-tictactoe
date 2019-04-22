package util

import java.time.LocalDateTime

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import de.heikoseeberger.akkahttpjson4s.Json4sSupport.ShouldWritePretty
import game.LobbyActor.{CreateRoom, JoinRoom, LobbyUpdate}
import game.domain.{Room, RoomStatusEnum}
import org.json4s.JsonAST.{JNull, JString}
import org.json4s.{CustomSerializer, DefaultFormats, Formats, NoTypeHints, TypeHints, native}

trait JsonSupport extends Json4sSupport {


  case class CustomTypeHints(custom: Map[Class[_], String]) extends TypeHints {
    val reverse: Map[String, Class[_]] = custom.map(_.swap)

    override val hints: List[Class[_]] = custom.keys.toList
    override def hintFor(clazz: Class[_]): String = custom.getOrElse(clazz, {
      throw new IllegalArgumentException(s"No type hint mapping found for $clazz")
    })
    override def classFor(hint: String): Option[Class[_]] = reverse.get(hint)
  }

  case object RoomStatusSerializer extends CustomSerializer[RoomStatusEnum.Value](format => (
    {
      case JString(p) => RoomStatusEnum.withName(p)
      case JNull      => null
    },
    {
      case status: RoomStatusEnum.Value => JString(status.toString)
    }
  ))

  case object LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format => (
    {
      case JString(p) => LocalDateTime.parse(p)
      case JNull      => null
    },
    {
      case date: LocalDateTime => JString(date.toString)
    }
  ))


  val customTypeHints = new CustomTypeHints(Map(
    classOf[LobbyUpdate] -> "lobby-update",
    classOf[CreateRoom] -> "create-room",
    classOf[Room] -> "room",
    classOf[JoinRoom] -> "join-room"
  ))

  implicit val shouldWritePretty: ShouldWritePretty = ShouldWritePretty.True
  implicit val json = native.Serialization
  implicit val formats = json.formats(customTypeHints) ++ List(
    LocalDateTimeSerializer,
    RoomStatusSerializer
  )

}
