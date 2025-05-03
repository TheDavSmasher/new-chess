package server.websocket;

import chess.ChessGame;
import static model.Serializer.*;
import model.dataaccess.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> userConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ArrayList<Connection>> connectionsToGames = new ConcurrentHashMap<>();

    public void addToGame(GameData gameData, String authToken, String username, Session session) {
        Connection newConnection = new Connection(username, session);
        if (!connectionsToGames.containsKey(gameData.gameID())) {
            connectionsToGames.put(gameData.gameID(), new ArrayList<>());
        }
        connectionsToGames.get(gameData.gameID()).add(newConnection);
        userConnections.put(authToken, newConnection);

        sendToConnection(userConnections.get(authToken), getGameString(gameData.game()));
    }

    public void removeFromGame(int gameID, String authToken) {
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);
        gameConnections.remove(getFromUsers(authToken));
        if (gameConnections.isEmpty()) {
            connectionsToGames.remove(gameID);
        } else {
            connectionsToGames.put(gameID, gameConnections);
        }
        userConnections.remove(authToken);
    }

    public Connection getFromUsers(String authToken) {
        return userConnections.get(authToken);
    }

    public void loadNewGame(ChessGame game, int gameID) {
        String message = getGameString(game);
        for (Connection current : connectionsToGames.get(gameID)) {
            sendToConnection(current, message);
        }
    }

    private String getGameString(ChessGame game) {
        String gameJson = serialize(game);
        return serialize(new LoadGameMessage(gameJson));
    }

    public void notifyGame(int gameID, Notification notification, String authToken) {
        ArrayList<Connection> closed = new ArrayList<>();
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);
        if (gameConnections == null) return;
        if (authToken == null) authToken = "";

        String message = serialize(notification);
        for (Connection current : gameConnections) {
            if (!current.isOpen()) {
                closed.add(current);
                continue;
            }
            if (current == userConnections.get(authToken)) continue;
            sendToConnection(current, message);
        }
        for (Connection close : closed) {
            gameConnections.remove(close);
        }
    }

    public void sendToConnection(Connection connection, String message) {
        try {
            connection.send(message);
        } catch (IOException ignored) {}
    }
}
