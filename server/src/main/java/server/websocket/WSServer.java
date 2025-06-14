package server.websocket;

import static model.Serializer.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.websocket.commands.*;
import websocket.commands.*;

import java.io.IOException;

@WebSocket
public class WSServer {
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand gameCommand = deserialize(message, UserGameCommand.class);
        (switch (gameCommand.getCommandType()) {
            case CONNECT -> new WSConnect();
            case MAKE_MOVE -> new WSMakeMove();
            case LEAVE -> new WSLeave();
            case RESIGN -> new WSResign();
        }).handle(message, session);
    }

    public static void send(Session session, Object message) {
        try {
            session.getRemote().sendString(serialize(message));
        } catch (IOException ignored) {}
    }
}
