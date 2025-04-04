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
        String username = enter(command.getAuthToken(), command.getGameID(), session);
        notifyGame(command.getGameID(), command.getAuthToken(), username + " is now observing the game.");
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
