package server.websocket.commands;

import chess.ChessGame;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import server.websocket.Connection;
import server.websocket.ConnectionManager;
import server.websocket.WebSocketCommand;
import service.GameService;
import websocket.commands.UserGameCommand;

public abstract class WSChessCommand<T extends UserGameCommand> extends WebSocketCommand<T> {
    protected static final String UNAUTHORIZED = "You are unauthorized.";

    protected WSChessCommand(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    protected ChessGame.TeamColor userIsPlayer(GameData data, String username) {
        if (data.whiteUsername() != null && data.whiteUsername().equals(username)) return ChessGame.TeamColor.WHITE;
        if (data.blackUsername() != null && data.blackUsername().equals(username)) return ChessGame.TeamColor.BLACK;
        return null;
    }

    protected void endGame(int gameID, String authToken, ChessGame game, String gameState) throws ServiceException {
        game.endGame();
        GameService.updateGameState(authToken, gameID, serialize(game));
        notifyGame(gameID, "The game has ended.\n" + gameState);
    }

    protected String CheckConnection(String authToken) throws ServiceException {
        Connection connection = connectionManager.getFromUsers(authToken);
        if (connection == null) {
            throw new ServiceException(UNAUTHORIZED);
        }
        return connection.username;
    }

    protected GameData CheckPlayerGameState(UserGameCommand command, String username, String description) throws ServiceException {
        GameData gameData = GameService.getGame(command.getAuthToken(), command.getGameID());
        if (!gameData.game().gameInPlay()) {
            throw new ServiceException("Game is already finished. You cannot " + description + " anymore.");
        }
        if (userIsPlayer(gameData, username) == null) {
            throw new ServiceException("You need to be a player to " + description + ".");
        }
        return gameData;
    }
}
