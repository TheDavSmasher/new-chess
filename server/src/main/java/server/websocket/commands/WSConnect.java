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

public class WSConnect extends WebSocketCommand<ConnectCommand> {
    public WSConnect(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<ConnectCommand> getCommandClass() {
        return ConnectCommand.class;
    }

    @Override
    protected void execute(ConnectCommand command, Session session) throws ServiceException {
        AuthData auth = UserService.validateAuth(command.getAuthToken());
        GameData data = GameService.getGame(command.getAuthToken(), command.getGameID());
        if (data == null) throw new ServiceException("Game does not exist.");

        notifyGame(command.getGameID(), auth.username() + " is now observing the game.");
        connectionManager.addToGame(command.getGameID(), command.getAuthToken(), auth.username(), session);
        connectionManager.loadNewGame(data.game(), command.getAuthToken());
    }
}
