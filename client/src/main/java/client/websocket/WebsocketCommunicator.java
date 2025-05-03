package client.websocket;

import chess.ChessMove;
import static model.Serializer.*;
import websocket.messages.*;
import websocket.commands.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebsocketCommunicator extends Endpoint {
    private final Session session;
    private final ServerMessageObserver observer;

    @SuppressWarnings("Convert2Lambda")
    public WebsocketCommunicator(String url, ServerMessageObserver messageObserver) throws IOException {
        try {
            observer = messageObserver;
            url = url.replace("http", "ws");
            URI socketURI = URI.create(url + "ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, socketURI);

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {
                        Class<? extends ServerMessage> messageClass =
                                switch (deserialize(message, ServerMessage.class).getServerMessageType()) {
                            case NOTIFICATION -> Notification.class;
                            case LOAD_GAME -> LoadGameMessage.class;
                            case ERROR -> ErrorMessage.class;
                        };
                        observer.notify(deserialize(message, messageClass));
                    } catch (Exception e) {
                        observer.notify(new ErrorMessage(e.getMessage()));
                    }
                }
            });
        } catch (DeploymentException | IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        //Method needed to call, but no functionality is required
    }

    public void connectToGame(String authToken, int gameID) throws IOException {
        sendCommand(new ConnectCommand(authToken, gameID));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        sendCommand(new MakeMoveCommand(authToken, gameID, move));
    }

    public void leaveGame(String authToken, int gameID) throws IOException {
        sendCommand(new LeaveCommand(authToken, gameID));
    }

    public void resignGame(String authToken, int gameID) throws IOException {
        sendCommand(new ResignCommand(authToken, gameID));
    }

    private void sendCommand(UserGameCommand command) throws IOException {
        session.getBasicRemote().sendText(serialize(command));
    }
}
