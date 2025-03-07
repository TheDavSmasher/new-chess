package client;

import model.dataaccess.GameData;
import model.response.CreateGameResponse;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    private final String username = "davhig";
    private final String password = "passTest";
    private final String email = "davhig@gmeia.com";
    private final String wrongUser = "dabhif";
    private final String wrongPassword = "shall-not-pass";
    private final String wrongAuth = "not-an-auth";
    private final String gameName = "gameName";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        ServerFacade.setPort(port);
    }

    @BeforeEach
    public void setUp() throws IOException {
        ServerFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerTest() {
        Assertions.assertDoesNotThrow(() ->
                Assertions.assertNotNull(ServerFacade.register(username, password, email).authToken()));
    }

    @Test
    public void registerFail() throws IOException {
        ServerFacade.register(username, password, email);

        Assertions.assertThrows(IOException.class, () -> ServerFacade.register("", password, email));
        Assertions.assertThrows(IOException.class, () -> ServerFacade.register(username, "", email));
        Assertions.assertThrows(IOException.class, () -> ServerFacade.register(username, "", email));
        Assertions.assertThrows(IOException.class, () -> ServerFacade.register(username, password, email));
    }

    @Test
    public void LoginTest() {
        Assertions.assertDoesNotThrow(() -> ServerFacade.register(username, password, email));

        Assertions.assertDoesNotThrow(() -> Assertions.assertNotNull(ServerFacade.login(username, password).authToken()));
    }

    @Test
    public void LoginFail() throws IOException {
        Assertions.assertThrows(IOException.class, () -> ServerFacade.login(username, password));

        ServerFacade.register(username, password, email);

        Assertions.assertThrows(IOException.class, () -> ServerFacade.login(username, wrongPassword));
        Assertions.assertThrows(IOException.class, () -> ServerFacade.login(wrongUser, password));
    }

    @Test
    public void ListGamesTest() throws IOException {
        String auth = ServerFacade.register(username, password, email).authToken();
        ServerFacade.createGame(auth, gameName);
        ArrayList<GameData> expected = new ArrayList<>();
        expected.add(new GameData(1, gameName, null));

        Assertions.assertEquals(expected, ServerFacade.listGames(auth));

        ServerFacade.createGame(auth, gameName);
        ServerFacade.createGame(auth, gameName);
        expected.add(new GameData(2, gameName, null));
        expected.add(new GameData(3, gameName, null));

        Assertions.assertEquals(expected, ServerFacade.listGames(auth));
    }

    @Test
    public void ListGamesFail() throws IOException {
        Assertions.assertThrows(IOException.class, () -> ServerFacade.listGames(wrongAuth));

        String auth = ServerFacade.register(username, password, email).authToken();

        Assertions.assertThrows(IOException.class, () -> ServerFacade.listGames(wrongAuth));
        Assertions.assertTrue(ServerFacade.listGames(auth).isEmpty());
    }

    @Test
    public void CreateGameTest() throws IOException {
        String auth = ServerFacade.register(username, password, email).authToken();
        Assertions.assertEquals(new CreateGameResponse(1), ServerFacade.createGame(auth, gameName));
        Assertions.assertEquals(new CreateGameResponse(2), ServerFacade.createGame(auth, gameName));
    }

    @Test
    public void CreateGameFail() throws IOException {
        Assertions.assertThrows(IOException.class, () -> ServerFacade.createGame(wrongAuth, gameName));
        String auth = ServerFacade.register(username, password, email).authToken();
        Assertions.assertThrows(IOException.class, () -> ServerFacade.createGame(wrongAuth, gameName));
        Assertions.assertThrows(IOException.class, () -> ServerFacade.createGame(auth, ""));
    }

    @Test
    public void JoinGameTest() throws IOException {
        String auth = ServerFacade.register(username, password, email).authToken();
        ServerFacade.createGame(auth, gameName);
        Assertions.assertDoesNotThrow(() -> ServerFacade.joinGame(auth, "white", 1));
    }

    @Test
    public void JoinGameFail() throws IOException {
        Assertions.assertThrows(IOException.class, () -> ServerFacade.joinGame(wrongAuth, "white", 1));

        String auth = ServerFacade.register(username, password, email).authToken();

        Assertions.assertThrows(IOException.class, () -> ServerFacade.joinGame(auth, "white", 1));

        ServerFacade.createGame(auth, gameName);

        Assertions.assertThrows(IOException.class, () -> ServerFacade.joinGame(auth, "white", 0));
        Assertions.assertThrows(IOException.class, () -> ServerFacade.joinGame(auth, "red", 1));

        ServerFacade.joinGame(auth, "white", 1);

        Assertions.assertThrows(IOException.class, () -> ServerFacade.joinGame(auth, "white", 0));
    }

    @Test
    public void LogoutTest() throws IOException {
        String tempAuth = ServerFacade.register(username, password, email).authToken();

        Assertions.assertDoesNotThrow(() -> ServerFacade.logout(tempAuth));

        String tempAuth2 = ServerFacade.login(username,password).authToken();

        Assertions.assertDoesNotThrow(() -> ServerFacade.logout(tempAuth2));
    }

    @Test
    public void LogoutFail() throws IOException {
        Assertions.assertThrows(IOException.class, () -> ServerFacade.logout(wrongAuth));

        String tempAuth = ServerFacade.register(username, password, email).authToken();
        ServerFacade.logout(tempAuth);

        Assertions.assertThrows(IOException.class, () -> ServerFacade.logout(tempAuth));
    }

    @Test
    public void ClearTest() {
        Assertions.assertDoesNotThrow(ServerFacade::clear);
    }
}
