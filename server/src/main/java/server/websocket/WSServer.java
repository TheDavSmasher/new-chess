package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.websocket.commands.*;
import websocket.commands.*;

import static server.websocket.WebSocketCommand.deserialize;

@WebSocket
public class WSServer {
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand gameCommand = deserialize(message);
        (switch (gameCommand.getCommandType()) {
            case CONNECT -> new WSConnect();
            case MAKE_MOVE -> new WSMakeMove();
            case LEAVE -> new WSLeave();
            case RESIGN -> new WSResign();
        }).handle(message, session);
    }
}
