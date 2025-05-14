package server.websocket;

import chess.ChessGame;
import static model.Serializer.*;
import model.dataaccess.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;

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

        newConnection.send(getLoadGame(gameData.game()));
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
        LoadGameMessage message = getLoadGame(game);
        for (Connection current : connectionsToGames.get(gameID)) {
            current.send(message);
        }
    }

    private LoadGameMessage getLoadGame(ChessGame game) {
        String gameJson = serialize(game);
        return new LoadGameMessage(gameJson);
    }

    public void notifyGame(int gameID, Notification notification, String authToken) {
        ArrayList<Connection> closed = new ArrayList<>();
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);
        if (gameConnections == null) return;
        if (authToken == null) authToken = "";

        for (Connection current : gameConnections) {
            if (!current.isOpen()) {
                closed.add(current);
                continue;
            }
            if (current == userConnections.get(authToken)) continue;
            current.send(notification);
        }
        for (Connection close : closed) {
            gameConnections.remove(close);
        }
    }
}
