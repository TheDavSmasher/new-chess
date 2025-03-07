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
import websocket.serverMessages.ErrorMessage;
import websocket.serverMessages.Notification;
import websocket.userCommands.*;

import java.io.IOException;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();

    private static <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    private static final String UNAUTHORIZED = "You are unauthorized.";

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand gameCommand = deserialize(message, UserGameCommand.class);
        switch (gameCommand.getCommandType()){
            case JOIN_PLAYER -> join(deserialize(message, JoinPlayerCommand.class), session);
            case JOIN_OBSERVER -> observe(deserialize(message, JoinObserverCommand.class), session);
            case MAKE_MOVE -> move(deserialize(message, MakeMoveCommand.class), session);
            case LEAVE -> leave(deserialize(message, LeaveCommand.class), session);
            case RESIGN -> resign(deserialize(message, ResignCommand.class), session);
        }
    }

    private final ConnectionManager connectionManager = new ConnectionManager();

    private void join(JoinPlayerCommand command, Session session) {
        try {
            String username = enter(command.getAuthString(), command.getGameID(), command.getColor(), session);
            Notification notification = new Notification(username + " has joined the game as " + command.getColor() + ".");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthString(), notification);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private void observe(JoinObserverCommand command, Session session) {
        try {
            String username = enter(command.getAuthString(), command.getGameID(), null,session);
            Notification notification = new Notification(username + " is now observing the game.");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthString(), notification);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private String enter(String authToken, int gameID, ChessGame.TeamColor color, Session session) throws ServiceException {
        AuthData auth = UserService.validateAuth(authToken);
        GameData data = GameService.getGame(authToken, gameID);
        if (data == null) throw new ServiceException("Game does not exist.");
        String user = color == ChessGame.TeamColor.WHITE ? data.whiteUsername() : data.blackUsername();
        if (color != null) {
            if (user == null) throw new ServiceException("RSVP the spot before calling this!");
            if (!user.equals(auth.username())) throw new ServiceException("Color is already taken.");
        }
        connectionManager.addToGame(gameID, authToken, auth.username(), session);
        connectionManager.loadNewGame(data.game(), authToken);
        return auth.username();
    }

    private void move(MakeMoveCommand command, Session session) {
        try {
            Connection connection = connectionManager.getFromUsers(command.getAuthString());
            if (connection == null) {
                sendError(session, UNAUTHORIZED);
                return;
            }
            GameData gameData = GameService.getGame(command.getAuthString(), command.getGameID());
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
            String gameJson = new Gson().toJson(game);
            GameService.updateGameState(command.getAuthString(), command.getGameID(), gameJson);

            connectionManager.loadNewGame(game, command.getGameID());
            ChessMove move = command.getMove();
            Notification moveNotification = new Notification(connection.username + " has moved piece at " +
                    positionAsString(move.getStartPosition()) + " to " + positionAsString(move.getEndPosition()) + ".");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthString(), moveNotification);

            String opponent = (game.getTeamTurn() == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
            if (game.isInCheckmate(game.getTeamTurn())) {
                Notification checkmateNotification = new Notification(opponent + " is now in checkmate.");
                connectionManager.notifyAll(command.getGameID(), checkmateNotification);
                endGame(command.getGameID(), command.getAuthString(), game, connection.username + " has won.");
            } else if (game.isInStalemate(game.getTeamTurn())) {
                Notification stalemateNotification = new Notification(opponent + " is now in stalemate.");
                connectionManager.notifyAll(command.getGameID(), stalemateNotification);
                endGame(command.getGameID(), command.getAuthString(), game, "The game is tied.");
            } else if (game.isInCheck(game.getTeamTurn())) {
                Notification checkNotification = new Notification(opponent + " is now in check.");
                connectionManager.notifyOthers(command.getGameID(), command.getAuthString(), checkNotification);
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
            Connection connection = connectionManager.getFromUsers(command.getAuthString());
            if (connection == null) {
                sendError(session, UNAUTHORIZED);
                return;
            }
            GameService.leaveGame(command.getAuthString(), command.getGameID());
            connectionManager.removeFromGame(command.getGameID(), command.getAuthString());
            Notification notification = new Notification(connection.username + " has left the game.");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthString(), notification);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private void resign(ResignCommand command, Session session) {
        try {
            Connection connection = connectionManager.getFromUsers(command.getAuthString());
            if (connection == null) {
                sendError(session, UNAUTHORIZED);
                return;
            }
            GameService.leaveGame(command.getAuthString(), command.getGameID());
            GameData gameData = GameService.getGame(command.getAuthString(), command.getGameID());
            if (!gameData.game().gameInPlay()) {
                sendError(session, "Game is already finished. You cannot resign anymore.");
                return;
            }
            if (userIsPlayer(gameData, connection.username) == null) {
                sendError(session, "You need to be a player to resign.");
                return;
            }
            endGame(command.getGameID(), command.getAuthString(), gameData.game(), connection.username + " has resigned the game.");
            connectionManager.removeFromGame(command.getGameID(), command.getAuthString());
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }

    private void endGame(int gameID, String authToken, ChessGame game, String gameState) throws ServiceException {
        game.endGame();
        String gameJson = new Gson().toJson(game);
        GameService.updateGameState(authToken, gameID, gameJson);
        Notification gameEnded = new Notification("The game has ended.\n" + gameState);
        connectionManager.notifyAll(gameID, gameEnded);
    }

    private void sendError(Session session, String message) {
        try {
            ErrorMessage error = new ErrorMessage(message);
            session.getRemote().sendString(new Gson().toJson(error));
        } catch (IOException ignored) {}
    }
}
