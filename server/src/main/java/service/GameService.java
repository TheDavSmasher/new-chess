package service;

import dataaccess.GameDAO;
import model.dataaccess.AuthData;
import model.dataaccess.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.response.CreateGameResponse;
import model.response.EmptyResponse;
import model.response.ListGamesResponse;
import model.response.result.*;

public class GameService extends Service {
    public static ListGamesResponse getAllGames(String authToken) throws ServiceException {
        return tryCatch(() -> {
            UserService.validateAuth(authToken);
            GameDAO gameDAO = GameDAO.getInstance();
            return new ListGamesResponse(gameDAO.listGames());
        });
    }

    public static CreateGameResponse createGame(CreateGameRequest request, String authToken) throws ServiceException {
        return tryCatch(() -> {
            UserService.validateAuth(authToken);
            GameDAO gameDAO = GameDAO.getInstance();

            if (request.gameName() == null || request.gameName().isEmpty()) {
                throw new BadRequestException();
            }
            GameData newGame = gameDAO.createGame(request.gameName());
            return new CreateGameResponse(newGame.gameID());
        });
    }

    public static EmptyResponse joinGame(JoinGameRequest request, String authToken) throws ServiceException {
        return tryCatch(() -> {
            AuthData auth = UserService.validateAuth(authToken);
            GameDAO gameDAO = GameDAO.getInstance();
            GameData oldGame = gameDAO.getGame(request.gameID());
            if (request.playerColor() == null || request.gameID() <= 0 || oldGame == null) {
                throw new BadRequestException();
            }
            String color = getColor(request.playerColor(), oldGame, auth.username());
            gameDAO.updateGamePlayer(request.gameID(), color, auth.username());
            return new EmptyResponse();
        });
    }

    public static GameData getGame(String authToken, int gameID) throws ServiceException {
        return tryCatch(() -> {
            UserService.validateAuth(authToken);
            GameDAO gameDAO = GameDAO.getInstance();
            return gameDAO.getGame(gameID);
        });
    }

    //WebSocket
    public static void leaveGame(String authToken, int gameID) throws ServiceException {
        tryCatch(() -> {
            AuthData auth = UserService.validateAuth(authToken);
            GameDAO gameDAO = GameDAO.getInstance();
            GameData oldGame = gameDAO.getGame(gameID);
            if (gameID <= 0 || oldGame == null) {
                throw new BadRequestException();
            }

            if (oldGame.whiteUsername() != null && oldGame.whiteUsername().equals(auth.username())) {
                gameDAO.updateGamePlayer(gameID, "WHITE", null);
            } else if (oldGame.blackUsername() != null && oldGame.blackUsername().equals(auth.username())) {
                gameDAO.updateGamePlayer(gameID, "BLACK", null);
            }
        });
    }

    public static void updateGameState(String authToken, int gameID, String gameJson) throws ServiceException {
        tryCatch(() -> {
            UserService.validateAuth(authToken);
            GameDAO gameDAO = GameDAO.getInstance();
            gameDAO.updateGameBoard(gameID, gameJson);
        });
    }

    private static String getColor(String playerColor, GameData oldGame, String username) throws ServiceException {
        String color = playerColor.toUpperCase();

        String gameUser = switch (color) {
            case "WHITE" -> oldGame.whiteUsername();
            case "BLACK" -> oldGame.blackUsername();
            default -> throw new BadRequestException();
        };

        if (gameUser == null || gameUser.equals(username)) {
            return color;
        }

        throw new PreexistingException();
    }
}
