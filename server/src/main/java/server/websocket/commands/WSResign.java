package server.websocket.commands;

import model.dataaccess.GameData;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
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
            GameData gameData = CheckPlayerGameState(command, username, "resign");
            GameService.leaveGame(command.getAuthToken(), command.getGameID());
            endGame(command.getGameID(), command.getAuthToken(), gameData.game(), username + " has resigned the game.");
            connectionManager.removeFromGame(command.getGameID(), command.getAuthToken());
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }
}
