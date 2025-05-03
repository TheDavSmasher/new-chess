package server.websocket;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;

import java.io.IOException;

public abstract class WebSocketCommand<T extends UserGameCommand> {
    protected static final ConnectionManager connectionManager = new ConnectionManager();

    protected abstract Class<T> getCommandClass();

    protected abstract void execute(T command, Session session) throws ServiceException, InvalidMoveException;

    public void handle(String message, Session session) {
        try {
            execute(deserialize(message, getCommandClass()), session);
        } catch (ServiceException | InvalidMoveException e) {
            try {
                ErrorMessage error = new ErrorMessage(e.getMessage());
                session.getRemote().sendString(serialize(error));
            } catch (IOException ignored) {}
        }
    }

    protected void notifyGame(int gameID, String message) {
        notifyGame(gameID, null, message);
    }

    protected void notifyGame(int gameID, String authToken, String message) {
        Notification notification = new Notification(message);
        connectionManager.notifyGame(gameID, notification, authToken);
    }

    //region Gson
    private static final Gson gson = new Gson();

    public static UserGameCommand deserialize(String json) {
        return deserialize(json, UserGameCommand.class);
    }

    protected static <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    protected static String serialize(Object object) {
        return gson.toJson(object);
    }
    //endregion
}
