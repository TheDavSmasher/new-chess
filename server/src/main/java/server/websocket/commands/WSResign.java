package server.websocket.commands;

import model.dataaccess.GameData;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.Connection;
import server.websocket.ConnectionManager;
import service.GameService;
import websocket.commands.ResignCommand;

public class WSResign extends WSChessCommand<ResignCommand> {
    public WSResign(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<ResignCommand> GetCommandClass() {
        return ResignCommand.class;
    }

    @Override
    protected void Execute(ResignCommand command, Session session) {
        try {
            String username = CheckConnection(command.getAuthToken());
            GameData gameData = GameService.getGame(command.getAuthToken(), command.getGameID());
            if (!gameData.game().gameInPlay()) {
                sendError(session, "Game is already finished. You cannot resign anymore.");
                return;
            }
            if (userIsPlayer(gameData, username) == null) {
                sendError(session, "You need to be a player to resign.");
                return;
            }
            GameService.leaveGame(command.getAuthToken(), command.getGameID());
            endGame(command.getGameID(), command.getAuthToken(), gameData.game(), username + " has resigned the game.");
            connectionManager.removeFromGame(command.getGameID(), command.getAuthToken());
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }
}
