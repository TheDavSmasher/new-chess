package client.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.serverMessages.*;
import websocket.userCommands.*;

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
            URI socketURI = URI.create(url + "connect");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, socketURI);

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {
                        Gson gson = new Gson();
                        switch (gson.fromJson(message, ServerMessage.class).getServerMessageType()) {
                            case NOTIFICATION -> observer.notify(gson.fromJson(message, Notification.class));
                            case LOAD_GAME -> observer.notify(gson.fromJson(message, LoadGameMessage.class));
                            case ERROR -> observer.notify(gson.fromJson(message, ErrorMessage.class));
                        }
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

    public void joinGame(String authToken, int gameID, String color) throws IOException {
        ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(color.toUpperCase());
        JoinPlayerCommand command = new JoinPlayerCommand(authToken, gameID, teamColor);
        sendCommand(new Gson().toJson(command));
    }

    public void observeGame(String authToken, int gameID) throws IOException {
        JoinObserverCommand command = new JoinObserverCommand(authToken, gameID);
        sendCommand(new Gson().toJson(command));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        sendCommand(new Gson().toJson(command));
    }

    public void leaveGame(String authToken, int gameID) throws IOException {
        LeaveCommand command = new LeaveCommand(authToken, gameID);
        sendCommand(new Gson().toJson(command));
    }

    public void resignGame(String authToken, int gameID) throws IOException {
        ResignCommand command = new ResignCommand(authToken, gameID);
        sendCommand(new Gson().toJson(command));
    }

    private void sendCommand(String jsonMessage) throws IOException {
        session.getBasicRemote().sendText(jsonMessage);
    }
}
