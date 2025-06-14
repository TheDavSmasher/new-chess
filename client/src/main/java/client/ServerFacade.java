package client;

import chess.ChessMove;
import client.websocket.ServerMessageObserver;
import client.websocket.WebsocketCommunicator;
import static model.Serializer.*;
import model.dataaccess.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.UserEnterRequest;
import model.response.CreateGameResponse;
import model.response.EmptyResponse;
import model.response.ListGamesResponse;
import model.response.UserEnterResponse;

import java.io.IOException;
import java.util.ArrayList;

public class ServerFacade {

    private static String urlPort = "http://localhost:8080/";
    private static WebsocketCommunicator websocket;

    public static void setPort(int port) {
        urlPort = "http://localhost:" + port + "/";
    }

    public static void setObserver(ServerMessageObserver observer) throws IOException {
        websocket = new WebsocketCommunicator(urlPort, observer);
    }

    public static UserEnterResponse register(String username, String password, String email) throws IOException {
        String url = urlPort + "user";
        String body = serialize(new UserEnterRequest(username, password, email));
        return HttpCommunicator.doPost(url, body, null, UserEnterResponse.class);
    }

    public static UserEnterResponse login(String username, String password) throws IOException {
        String url = urlPort + "session";
        String body = serialize(new UserEnterRequest(username, password, null));
        return HttpCommunicator.doPost(url, body, null, UserEnterResponse.class);
    }

    public static ArrayList<GameData> listGames(String authToken) throws IOException {
        ListGamesResponse response = HttpCommunicator.doGet(urlPort + "game", authToken, ListGamesResponse.class);
        return response.games();
    }

    public static CreateGameResponse createGame(String authToken, String gameName) throws IOException {
        String url = urlPort + "game";
        String body = serialize(new CreateGameRequest(gameName));
        return HttpCommunicator.doPost(url, body, authToken, CreateGameResponse.class);
    }

    public static void joinGame(String authToken, String color, int gameID) throws IOException {
        String url = urlPort + "game";
        String body = serialize(new JoinGameRequest(color, gameID));
        HttpCommunicator.doPut(url, body, authToken, EmptyResponse.class);
    }

    public static void logout(String authToken) throws IOException {
        String url = urlPort + "session";
        HttpCommunicator.doDelete(url, authToken, EmptyResponse.class);
    }

    public static void clear() throws IOException {
        String url = urlPort + "db";
        HttpCommunicator.doDelete(url, null, EmptyResponse.class);
    }

    //Websocket
    public static void connectToGame(String authToken, int gameID) throws IOException {
        websocket.connectToGame(authToken, gameID);
    }

    public static void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        websocket.makeMove(authToken, gameID, move);
    }

    public static void leaveGame(String authToken, int gameID) throws IOException {
        websocket.leaveGame(authToken, gameID);
    }

    public static void resignGame(String authToken, int gameID) throws IOException {
        websocket.resignGame(authToken, gameID);
    }
}
