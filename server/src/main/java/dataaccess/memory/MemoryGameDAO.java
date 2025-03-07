package dataaccess.memory;

import dataaccess.GameDAO;
import model.dataaccess.GameData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {
    static MemoryGameDAO instance;
    private final HashSet<GameData> data;

    MemoryGameDAO() {
        data = new HashSet<>();
    }
    @Override
    public ArrayList<GameData> listGames() {
        ArrayList<GameData> gameList = new ArrayList<>();
        for (GameData game : data) {
            gameList.add(new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }
        gameList.sort(Comparator.comparingInt(GameData::gameID));
        return gameList;
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : data) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public GameData createGame(String gameName) {
        int newID = data.size() + 1;
        GameData newGame = new GameData(newID, gameName);
        data.add(newGame);
        return newGame;
    }

    @Override
    public void updateGamePlayer(int gameID, String color, String username) {
        GameData oldGame = getGame(gameID);
        deleteGame(gameID);
        if (color.equals("WHITE")) {
            data.add(new GameData(oldGame.gameID(), username, oldGame.blackUsername(), oldGame.gameName(), oldGame.game()));
        } else {
            data.add(new GameData(oldGame.gameID(), oldGame.whiteUsername(), username, oldGame.gameName(), oldGame.game()));
        }
    }

    @Override
    public void updateGameBoard(int gameID, String gameJson) {

    }

    private void deleteGame(int gameID) {
        data.removeIf(game -> game.gameID() == gameID);
    }

    @Override
    public void clear() {
        data.clear();
    }

    public static GameDAO getInstance() {
        if (instance == null) {
            instance = new MemoryGameDAO();
        }
        return instance;
    }
}
