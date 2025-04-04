package server.websocket.commands;

import chess.ChessGame;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import server.websocket.Connection;
import server.websocket.ConnectionManager;
import server.websocket.WebSocketCommand;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;

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
        String gameJson = serialize(game);
        GameService.updateGameState(authToken, gameID, gameJson);
        Notification gameEnded = new Notification("The game has ended.\n" + gameState);
        connectionManager.notifyAll(gameID, gameEnded);
    }

    protected String CheckConnection(String authToken) throws ServiceException {
        Connection connection = connectionManager.getFromUsers(authToken);
        if (connection == null) {
            throw new ServiceException(UNAUTHORIZED);
        }
        return connection.username;
    }
}
