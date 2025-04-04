package server.websocket;

import com.google.gson.Gson;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

import java.io.IOException;

public abstract class WebSocketCommand<T extends UserGameCommand> {
    protected final ConnectionManager connectionManager;

    protected WebSocketCommand(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    protected abstract Class<T> GetCommandClass();

    protected abstract void Execute(T command, Session session) throws ServiceException;

    public void handle(String message, Session session) {
        try {
            Execute(deserialize(message, GetCommandClass()), session);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    protected void sendError(Session session, String message) {
        try {
            ErrorMessage error = new ErrorMessage(message);
            session.getRemote().sendString(serialize(error));
        } catch (IOException ignored) {}
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
