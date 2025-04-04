package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.websocket.commands.WSConnect;
import server.websocket.commands.WSLeave;
import server.websocket.commands.WSMakeMove;
import server.websocket.commands.WSResign;
import websocket.commands.*;

import static server.websocket.WebSocketCommand.deserialize;

@WebSocket
public class WSServer {
    private final ConnectionManager connectionManager = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand gameCommand = deserialize(message);
        (switch (gameCommand.getCommandType()) {
            case CONNECT -> new WSConnect(connectionManager);
            case MAKE_MOVE -> new WSMakeMove(connectionManager);
            case LEAVE -> new WSLeave(connectionManager);
            case RESIGN -> new WSResign(connectionManager);
        }).handle(message, session);
    }
}
