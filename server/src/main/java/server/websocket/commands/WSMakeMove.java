package server.websocket.commands;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import service.GameService;
import websocket.commands.MakeMoveCommand;

public class WSMakeMove extends WSChessCommand<MakeMoveCommand> {
    public WSMakeMove(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<MakeMoveCommand> getCommandClass() {
        return MakeMoveCommand.class;
    }

    @Override
    protected void execute(MakeMoveCommand command, Session session) throws ServiceException, InvalidMoveException {
        String username = CheckConnection(command.getAuthToken());
        GameData gameData = checkPlayerGameState(command, username, "make a move");

        ChessGame game = gameData.game();
        ChessMove move = command.getMove();
        game.makeMove(move);
        GameService.updateGameState(command.getAuthToken(), command.getGameID(), serialize(game));

        connectionManager.loadNewGame(game, command.getGameID());

        notifyGame(command.getGameID(), command.getAuthToken(), username + " has moved piece at " +
                positionAsString(move.getStartPosition()) + " to " + positionAsString(move.getEndPosition()) + ".");

        ChessGame.TeamColor currentTurn = game.getTeamTurn();
        String opponent = (currentTurn == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
        switch (game.getCheckState(currentTurn)) {
            case CHECKMATE -> endGame(command, game, opponent + " is now in checkmate.\n" + username + " has won.");
            case STALEMATE -> endGame(command, game, opponent + " is now in stalemate.\nThe game is tied.");
            case CHECK -> notifyGame(command.getGameID(), opponent + " is now in check.");
        }
    }

    private String positionAsString(ChessPosition position) {
        String end = "";
        switch (position.getColumn()) {
            case 1 -> end += "A";
            case 2 -> end += "B";
            case 3 -> end += "C";
            case 4 -> end += "D";
            case 5 -> end += "E";
            case 6 -> end += "F";
            case 7 -> end += "G";
            default -> end += "H";
        }
        end += position.getRow();
        return end;
    }
}
