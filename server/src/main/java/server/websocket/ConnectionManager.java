package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> userConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ArrayList<Connection>> connectionsToGames = new ConcurrentHashMap<>();

    private void addToUsers(String authToken, Connection connection) {
        userConnections.put(authToken, connection);
    }

    private void removeFromUser(String authToken) {
        userConnections.remove(authToken);
    }

    public void addToGame(int gameID, String authToken, String username, Session session) {
        Connection newConnection = new Connection(username, session);
        if (!connectionsToGames.containsKey(gameID)) {
            connectionsToGames.put(gameID, new ArrayList<>());
        }
        connectionsToGames.get(gameID).add(newConnection);
        addToUsers(authToken, newConnection);
    }

    public void removeFromGame(int gameID, String authToken) {
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);
        gameConnections.remove(getFromUsers(authToken));
        connectionsToGames.put(gameID, gameConnections);
        removeFromUser(authToken);
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

    public void loadNewGame(ChessGame game, String authToken) {
        String message = getGameString(game);
        sendToConnection(userConnections.get(authToken), message);
    }

    private String getGameString(ChessGame game) {
        Gson gson = new Gson();
        String gameJson = gson.toJson(game);
        return gson.toJson(new LoadGameMessage(gameJson));
    }

    public void notifyOthers(int gameID, String authToken, Notification notification) {
        ArrayList<Connection> closed = new ArrayList<>();
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);

        String message = new Gson().toJson(notification);
        for (Connection current : gameConnections) {
            if (!current.session.isOpen()) {
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

    public void notifyAll(int gameID, Notification notification) {
        ArrayList<Connection> closed = new ArrayList<>();
        ArrayList<Connection> gameConnections = connectionsToGames.get(gameID);
        String message = new Gson().toJson(notification);
        for (Connection current : gameConnections) {
            if (current.session.isOpen()) {
                sendToConnection(current, message);
            } else {
                closed.add(current);
            }
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
