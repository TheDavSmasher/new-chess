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
        ChessGame game = checkPlayerGameState(command, username).game();
        notifyGame(command.getGameID(), endGame(command, game) + username + " has resigned the game.");
    }
}
