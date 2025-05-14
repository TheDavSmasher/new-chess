package server.websocket;

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
        int gameID = gameData.gameID();
        if (!connectionsToGames.containsKey(gameID)) {
            connectionsToGames.put(gameID, new ArrayList<>());
        }
        connectionsToGames.get(gameID).add(newConnection);
        userConnections.put(authToken, newConnection);

        newConnection.send(getLoadGame(gameData));
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

    public void loadNewGame(GameData gameData, int gameID) {
        LoadGameMessage message = getLoadGame(gameData);
        for (Connection current : connectionsToGames.get(gameID)) {
            current.send(message);
        }
    }

    private LoadGameMessage getLoadGame(GameData gameData) {
        return new LoadGameMessage(serialize(gameData.game()));
    }

    public void notifyGame(int gameID, Notification notification, String authToken) {
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);
        if (gameConnections == null) return;
        if (authToken == null) authToken = "";

        gameConnections.removeIf(c -> !c.isOpen());

        for (Connection current : gameConnections) {
            if (current == userConnections.get(authToken)) continue;
            current.send(notification);
        }
    }
}
