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
import websocket.messages.Notification;

public class WSMakeMove extends WSChessCommand<MakeMoveCommand> {
    public WSMakeMove(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<MakeMoveCommand> GetCommandClass() {
        return MakeMoveCommand.class;
    }

    @Override
    protected void Execute(MakeMoveCommand command, Session session) {
        try {
            String username = CheckConnection(command.getAuthToken());
            GameData gameData = CheckPlayerGameState(command, username, "make a move");

            ChessGame game = gameData.game();
            if (userIsPlayer(gameData, username) != game.getTeamTurn()) {
                sendError(session, "It is not your turn to make a move.");
                return;
            }
            game.makeMove(command.getMove());
            String gameJson = serialize(game);
            GameService.updateGameState(command.getAuthToken(), command.getGameID(), gameJson);

            connectionManager.loadNewGame(game, command.getGameID());
            ChessMove move = command.getMove();
            Notification moveNotification = new Notification(username + " has moved piece at " +
                    positionAsString(move.getStartPosition()) + " to " + positionAsString(move.getEndPosition()) + ".");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), moveNotification);

            String opponent = (game.getTeamTurn() == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
            if (game.isInCheckmate(game.getTeamTurn())) {
                endGame(command.getGameID(), command.getAuthToken(), game, opponent + " is now in checkmate.\n" + username + " has won.");
            } else if (game.isInStalemate(game.getTeamTurn())) {
                endGame(command.getGameID(), command.getAuthToken(), game, opponent + " is now in stalemate.\nThe game is tied.");
            } else if (game.isInCheck(game.getTeamTurn())) {
                Notification checkNotification = new Notification(opponent + " is now in check.");
                connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), checkNotification);
            }
        } catch (ServiceException | InvalidMoveException e) {
            sendError(session, e.getMessage());
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
