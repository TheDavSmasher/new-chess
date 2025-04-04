package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.dataaccess.AuthData;
import model.dataaccess.GameData;
import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.commands.*;

import java.io.IOException;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();

    private static <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    private static String serialize(Object object) {
        return gson.toJson(object);
    }

    private static final String UNAUTHORIZED = "You are unauthorized.";

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand gameCommand = deserialize(message, UserGameCommand.class);
        switch (gameCommand.getCommandType()){
            case CONNECT -> connect(deserialize(message, ConnectCommand.class), session);
            case MAKE_MOVE -> move(deserialize(message, MakeMoveCommand.class), session);
            case LEAVE -> leave(deserialize(message, LeaveCommand.class), session);
            case RESIGN -> resign(deserialize(message, ResignCommand.class), session);
        }
    }

    private final ConnectionManager connectionManager = new ConnectionManager();
    
    private void connect(ConnectCommand command, Session session) {
        try {
            String username = enter(command.getAuthToken(), command.getGameID(), session);
            Notification notification = new Notification(username + " is now observing the game.");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), notification);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private String enter(String authToken, int gameID, Session session) throws ServiceException {
        AuthData auth = UserService.validateAuth(authToken);
        GameData data = GameService.getGame(authToken, gameID);
        if (data == null) throw new ServiceException("Game does not exist.");
        connectionManager.addToGame(gameID, authToken, auth.username(), session);
        connectionManager.loadNewGame(data.game(), authToken);
        return auth.username();
    }

    private void move(MakeMoveCommand command, Session session) {
        try {
            Connection connection = connectionManager.getFromUsers(command.getAuthToken());
            if (connection == null) {
                sendError(session, UNAUTHORIZED);
                return;
            }
            GameData gameData = GameService.getGame(command.getAuthToken(), command.getGameID());
            if (userIsPlayer(gameData, connection.username) == null) {
                sendError(session, "You need to be a player to make a move");
                return;
            }
            ChessGame game = gameData.game();
            if (userIsPlayer(gameData, connection.username) != game.getTeamTurn()) {
                sendError(session, "It is not your turn to make a move.");
                return;
            }
            game.makeMove(command.getMove());
            String gameJson = serialize(game);
            GameService.updateGameState(command.getAuthToken(), command.getGameID(), gameJson);

            connectionManager.loadNewGame(game, command.getGameID());
            ChessMove move = command.getMove();
            Notification moveNotification = new Notification(connection.username + " has moved piece at " +
                    positionAsString(move.getStartPosition()) + " to " + positionAsString(move.getEndPosition()) + ".");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), moveNotification);

            String opponent = (game.getTeamTurn() == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
            if (game.isInCheckmate(game.getTeamTurn())) {
                endGame(command.getGameID(), command.getAuthToken(), game, opponent + " is now in checkmate.\n" + connection.username + " has won.");
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

    private ChessGame.TeamColor userIsPlayer(GameData data, String username) {
        if (data.whiteUsername() != null && data.whiteUsername().equals(username)) return ChessGame.TeamColor.WHITE;
        if (data.blackUsername() != null && data.blackUsername().equals(username)) return ChessGame.TeamColor.BLACK;
        return null;
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

    private void leave(LeaveCommand command, Session session) {
        try {
            Connection connection = connectionManager.getFromUsers(command.getAuthToken());
            if (connection == null) {
                sendError(session, UNAUTHORIZED);
                return;
            }
            GameService.leaveGame(command.getAuthToken(), command.getGameID());
            connectionManager.removeFromGame(command.getGameID(), command.getAuthToken());
            Notification notification = new Notification(connection.username + " has left the game.");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), notification);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private void resign(ResignCommand command, Session session) {
        try {
            Connection connection = connectionManager.getFromUsers(command.getAuthToken());
            if (connection == null) {
                sendError(session, UNAUTHORIZED);
                return;
            }
            GameData gameData = GameService.getGame(command.getAuthToken(), command.getGameID());
            if (!gameData.game().gameInPlay()) {
                sendError(session, "Game is already finished. You cannot resign anymore.");
                return;
            }
            if (userIsPlayer(gameData, connection.username) == null) {
                sendError(session, "You need to be a player to resign.");
                return;
            }
            GameService.leaveGame(command.getAuthToken(), command.getGameID());
            endGame(command.getGameID(), command.getAuthToken(), gameData.game(), connection.username + " has resigned the game.");
            connectionManager.removeFromGame(command.getGameID(), command.getAuthToken());
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private void endGame(int gameID, String authToken, ChessGame game, String gameState) throws ServiceException {
        game.endGame();
        String gameJson = serialize(game);
        GameService.updateGameState(authToken, gameID, gameJson);
        Notification gameEnded = new Notification("The game has ended.\n" + gameState);
        connectionManager.notifyAll(gameID, gameEnded);
    }

    private void sendError(Session session, String message) {
        try {
            ErrorMessage error = new ErrorMessage(message);
            session.getRemote().sendString(serialize(error));
        } catch (IOException ignored) {}
    }
}
