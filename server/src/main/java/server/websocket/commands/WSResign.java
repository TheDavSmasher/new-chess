package server.websocket.commands;

import chess.ChessGame;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import websocket.commands.ResignCommand;

public class WSResign extends WSChessCommand<ResignCommand> {
    public WSResign(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<ResignCommand> getCommandClass() {
        return ResignCommand.class;
    }

    @Override
    protected void execute(ResignCommand command, Session session) throws ServiceException {
        String username = CheckConnection(command.getAuthToken());
        ChessGame game = CheckPlayerGameState(command, username, "resign").game();
        endGame(command.getGameID(), command.getAuthToken(), game, username + " has resigned the game.");
    }
}
