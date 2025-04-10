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

    protected void endGame(UserGameCommand command, ChessGame game, String gameState) throws ServiceException {
        game.endGame();
        GameService.updateGameState(command.getAuthToken(), command.getGameID(), serialize(game));
        notifyGame(command.getGameID(), "The game has ended.\n" + gameState);
    }

    protected String CheckConnection(String authToken) throws ServiceException {
        Connection connection = connectionManager.getFromUsers(authToken);
        if (connection == null) {
            throw new ServiceException(UNAUTHORIZED);
        }
        return connection.username;
    }

    protected GameData checkPlayerGameState(UserGameCommand command, String username) throws ServiceException {
        return checkPlayerGameState(command, username, "resign");
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
        if (data.whiteUsername() != null && data.whiteUsername().equals(username)) return ChessGame.TeamColor.WHITE;
        if (data.blackUsername() != null && data.blackUsername().equals(username)) return ChessGame.TeamColor.BLACK;
        return null;
    }
}
