package server.websocket.commands;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import websocket.commands.MakeMoveCommand;

public class WSMakeMove extends WSChessCommand<MakeMoveCommand> {
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
        ChessGame.CheckState state = game.getCheckState(currentTurn);
        if (state == ChessGame.CheckState.NONE) return;

        String opponent = (currentTurn == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
        String message = opponent + " is now in " + state.name().toLowerCase() + ".";
        if (state != ChessGame.CheckState.CHECK) {
            message = endGame(command, game) + message + "\n";
            message += state == ChessGame.CheckState.STALEMATE ? "The game is tied." : username + " has won.";
        }
        notifyGame(command.getGameID(), message);
    }

    private String positionAsString(ChessPosition position) {
        return String.valueOf('A' + position.getColumn() - 1) + (position.getRow() - 1);
    }
}
