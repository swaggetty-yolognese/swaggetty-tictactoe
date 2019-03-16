package example

object Wire {

  trait SocketMessage

  class InboundSocketMessage extends SocketMessage
  case class GetRoom(roomId: String) extends InboundSocketMessage
  case object CreateRoom extends InboundSocketMessage

  class OutboundSocketMessage extends SocketMessage
  case class RoomCreated(roomId: String) extends OutboundSocketMessage
  case class GenericMessage(msg: String) extends OutboundSocketMessage

}
