package server.websocket.commands;

import model.dataaccess.AuthData;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import server.websocket.WebSocketCommand;
import service.GameService;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.messages.Notification;

public class WSConnect extends WebSocketCommand<ConnectCommand> {
    public WSConnect(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<ConnectCommand> GetCommandClass() {
        return ConnectCommand.class;
    }

    @Override
    protected void Execute(ConnectCommand command, Session session) throws ServiceException {
        String username = enter(command.getAuthToken(), command.getGameID(), session);
        Notification notification = new Notification(username + " is now observing the game.");
        connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), notification);
    }

    private String enter(String authToken, int gameID, Session session) throws ServiceException {
        AuthData auth = UserService.validateAuth(authToken);
        GameData data = GameService.getGame(authToken, gameID);
        if (data == null) throw new ServiceException("Game does not exist.");
        connectionManager.addToGame(gameID, authToken, auth.username(), session);
        connectionManager.loadNewGame(data.game(), authToken);
        return auth.username();
    }
}
