package server.websocket.commands;

import chess.ChessGame;
import static model.Serializer.*;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import server.websocket.Connection;
import server.websocket.WebSocketCommand;
import service.GameService;
import websocket.commands.UserGameCommand;

public abstract class WSChessCommand<T extends UserGameCommand> extends WebSocketCommand<T> {
    protected static final String UNAUTHORIZED = "You are unauthorized.";

    protected String endGame(UserGameCommand command, ChessGame game) throws ServiceException {
        game.endGame();
        GameService.updateGameState(command.getAuthToken(), command.getGameID(), serialize(game));
        return "The game has ended.\n";
    }

    protected String CheckConnection(String authToken) throws ServiceException {
        Connection connection = connectionManager.getFromUsers(authToken);
        if (connection == null) {
            throw new ServiceException(UNAUTHORIZED);
        }
        return connection.username();
    }

    protected GameData checkPlayerGameState(UserGameCommand command, String username, String description) throws ServiceException {
        boolean checkColor = !description.equals("resign");
        GameData gameData = GameService.getGame(command.getAuthToken(), command.getGameID());
        if (gameData.game().isGameOver()) {
            throw new ServiceException("Game is already finished. You cannot " + description + " anymore.");
        }
        ChessGame.TeamColor color = userIsPlayer(gameData, username);
        if (color == null) {
            throw new ServiceException("You need to be a player to " + description + ".");
        }
        if (checkColor && color != gameData.game().getTeamTurn()) {
            throw new ServiceException("It is not your turn to make a move.");
        }
        return gameData;
    }

    private ChessGame.TeamColor userIsPlayer(GameData data, String username) {
        if (username.equals(data.whiteUsername())) return ChessGame.TeamColor.WHITE;
        if (username.equals(data.blackUsername())) return ChessGame.TeamColor.BLACK;
        return null;
    }
}
