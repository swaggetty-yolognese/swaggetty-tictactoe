# Documentation

### Nomenclature

- Lobby: Supervises all the rooms, can create, delete and enumerate rooms.
- Room: A tic-tac-toe _match_. Rooms have initially only one player (status=WAITING) and
when an opponent picks up the challenge it joins the room bringing it to status=ACTIVE. The
game can have 3 outcomes: player1 win and player2 loses, viceversa of the previous and DRAW, once
the room enters a final state it can't go back.

## Lobby

The lobby is a facility that keeps track of all the rooms, it is accessible via the 
websocket at `GET ws://localhost/lobby/ws`. The socket broadcasts to all connected clients
the status of all non-final state rooms, so WAITING and ACTIVE rooms. Via the websocket is
possible to create a new room (newly created rooms have status=WAITING), room creation starts
with a new player willing to _wait_ for opponents to join (todo: payment flow). 

### Joining a room

When a player wants to become an opponent in an existing room, the lobby expects an HTTP call
at `POST http://localhost/lobby/join` passing as body a [join-room message](#join-room-message), the API is 
**synchronous** and if the join operation goes well it returns unique endpoint URL to be used
to interact with the room.

### Playing

When players join a room they receive a unique URL that exist only for the lifespan of the room 
itself, the URL exposes a websocket with a very high frequency update rate. Through this websocket
players can submit their moves and asynchronously receive the board state (used to update the view?).
All operations here are non blocking and any player can submit a move at any time, there is 
no error response; instead if a player tries to make an illegal move it will **just** not be added
to the board. This mechanism ensures that every input from clients can be safely handled by the backend
without client-side validation or sanitation, likewise all the output from the backend can be safely
displayed on the client.


## Response structure
All data is json encoded and contains a field `jsonClass` that identifies the actual type of data 
being exchanged.


### lobby status message

Broadcast at regular interval to all clients connected at `GET ws://localhost/lobby/ws`

```json
{
   "jsonClass":"lobby-update",
   "date":"2019-04-22T15:25:37.389166",
   "rooms":[
      {
         "jsonClass":"room",
         "roomId":"74ffa3c0-b49e-4517-9b71-e4015218a7ea",
         "player1":{
            "name":"Andrea"
         },
         "status":"WAITING"
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

### join-room-message

Sent by a client to join an existing room:

```json
{
	"jsonClass": "join-room",
	"roomId": "86ccd31a-186f-47bf-9d7e-e13520c8f3db",
	"player": "Hal Finney"
}
```

