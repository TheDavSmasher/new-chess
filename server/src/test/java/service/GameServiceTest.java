package service;

import model.dataaccess.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.UserEnterRequest;
import model.response.CreateGameResponse;
import model.response.ListGamesResponse;
import model.response.result.BadRequestException;
import model.response.result.ServiceException;
import model.response.result.UnauthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class GameServiceTest {

    String authToken;
    String wrongAuthToken = "not-an-auth-token";

    @BeforeEach
    public void setUp() throws ServiceException {
        AppService.clearData();
        authToken = UserService.register(new UserEnterRequest("davhig22", "pass123", "davhig22@byu.edu")).authToken();
    }

    @Test
    public void listGamesTest() throws ServiceException {
        ListGamesResponse expected = new ListGamesResponse(new ArrayList<>());
        Assertions.assertEquals(expected, GameService.getAllGames(authToken));

        ArrayList<GameData> gamesList = new ArrayList<>();
        gamesList.add(GameData.testEmpty(1, "game_1"));
        expected = new ListGamesResponse(gamesList);
        GameService.createGame(new CreateGameRequest("game_1"), authToken);
        Assertions.assertEquals(expected, GameService.getAllGames(authToken));

        gamesList.add(GameData.testEmpty(2, "game_2"));
        gamesList.add(GameData.testEmpty(3, "game_3"));
        expected = new ListGamesResponse(gamesList);
        GameService.createGame(new CreateGameRequest("game_2"), authToken);
        GameService.createGame(new CreateGameRequest("game_3"), authToken);
        Assertions.assertEquals(expected, GameService.getAllGames(authToken));
    }

    @Test
    public void listGamesFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> GameService.getAllGames(wrongAuthToken));
    }

    @Test
    public void createGameTest() throws ServiceException {
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");
        Assertions.assertEquals(new CreateGameResponse(1), GameService.createGame(createGameRequest, authToken));

        createGameRequest = new CreateGameRequest("gameName");
        Assertions.assertEquals(new CreateGameResponse(2), GameService.createGame(createGameRequest, authToken));
    }

    @Test
    public void createGameFail() {
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");

        Assertions.assertThrows(UnauthorizedException.class, () -> GameService.createGame(createGameRequest, wrongAuthToken));

        CreateGameRequest nullRequest = new CreateGameRequest(null);
        Assertions.assertThrows(BadRequestException.class, () -> GameService.createGame(nullRequest, authToken));

        CreateGameRequest badRequest = new CreateGameRequest("");
        Assertions.assertThrows(BadRequestException.class, () -> GameService.createGame(badRequest, authToken));
    }

    @Test
    public void joinGameTest() throws ServiceException {
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", 1);

        GameService.createGame(new CreateGameRequest("gameName"), authToken);

        Assertions.assertDoesNotThrow(() -> GameService.joinGame(joinGameRequest, authToken));
    }

    @Test
    public void joinGameFail() throws ServiceException {
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", 1);

        GameService.createGame(new CreateGameRequest("gameName"), authToken);
        Assertions.assertThrows(UnauthorizedException.class, () -> GameService.joinGame(joinGameRequest, wrongAuthToken));

        JoinGameRequest badColorRequest = new JoinGameRequest("yellow", 1);
        Assertions.assertThrows(BadRequestException.class, () -> GameService.joinGame(badColorRequest, authToken));

        JoinGameRequest nullGameRequest = new JoinGameRequest("WHITE", 2);
        Assertions.assertThrows(BadRequestException.class, () -> GameService.joinGame(nullGameRequest, authToken));
    }
}