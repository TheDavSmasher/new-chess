package server.websocket;

import chess.InvalidMoveException;
import static model.Serializer.*;
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
}
