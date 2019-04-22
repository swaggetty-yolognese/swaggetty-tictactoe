# Documentation

### Nomenclature

- Lobby: Supervises all the rooms, can create, delete and enumerate rooms.
- Room: A tic-tac-toe _match_. Rooms have initially only one player (status: WAITING) and
when an opponend picks up the challenge it joins the room bringig it to status: ACTIVE. The
game can have 3 outcomes: player1 win (player2 loses), viceversa of the previous and DRAW, once
the room enters a final state it can't go back.

## Lobby

The lobby is a facility that keeps track of all the rooms, it is accessible via its 
websocket at `GET ws://localhost/lobby/ws`. The socket broadcasts to all connected clients
the status of all non-final state rooms, so WAITING and ACTIVE rooms. Via the websocket is
possible to create a new room (newly created rooms have status=WAITING), room creation starts
with a new player willing to _wait_ for opponents to join (todo: payment flow).


## Response structure
All data is json encoded and contains a field `jsonClass` that identifies the actual type of data 
being exchanged.


### lobby status message

Broadcast at regular interval to all clients connected at `GET ws://localhost/lobby/ws`

```json
{
   "jsonClass":"lobby-update",
   "date":"2019-04-22T14:38:01.600629",
   "rooms":[
      {
         "jsonClass":"room",
         "roomId":"300d4d6d-6d83-462c-a153-294610c9d318",
         "player1":{
            "name":"Andrea"
         }
      }
   ]
}
``` 

### create room message

Sent by a client to create a new room:

```json
{
   "jsonClass":"create-room",
   "player1":"Satoshi"
}
```


