package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.dataaccess.GameData;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLGameDAO extends SQLDAO implements GameDAO {
    private static SQLGameDAO instance;

    public SQLGameDAO () throws DataAccessException {
        configureDatabase();
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> gameList = new ArrayList<>();
        tryStatement("SELECT gameID, whiteUsername, blackUsername, gameName FROM games", preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("gameID");
                    String white = rs.getString("whiteUsername");
                    String black = rs.getString("blackUsername");
                    String name = rs.getString("gameName");

                    gameList.add(new GameData(id, white, black, name));
                }
                return null;
            }
        });
        return gameList;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return tryStatement("SELECT * FROM games WHERE gameID =?", preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) return null;
                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name = rs.getString("gameName");
                String gameJson = rs.getString("game");
                ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);

                return new GameData(id, white, black, name, game);
            }
        }, gameID);
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        ChessGame game = new ChessGame();
        String gameJson = new Gson().toJson(game);
        AtomicInteger id = new AtomicInteger();
        tryUpdate("INSERT INTO games (gameName, game) VALUES (?, ?)", updateResKey -> {
            SQLDAO.confirmUpdate(updateResKey);
            id.set(updateResKey);
        }, gameName, gameJson);
        return new GameData(id.get(), gameName, game);
    }

    @Override
    public void updateGamePlayer(int gameID, String color, String username) throws DataAccessException {
        tryUpdate("UPDATE games SET "+ (color.equals("WHITE") ? "whiteUsername" : "blackUsername") +"=? WHERE gameID=?", SQLDAO::confirmUpdate, username, gameID);
    }

    @Override
    public void updateGameBoard(int gameID, String gameJson) throws DataAccessException {
        tryUpdate("UPDATE games SET game=? WHERE gameID=?", SQLDAO::confirmUpdate, gameJson, gameID);
    }

    @Override
    public void clear() throws DataAccessException {
        tryUpdate("TRUNCATE games", SQLDAO::cleared);
    }

    public static GameDAO getInstance() throws DataAccessException {
        if (instance == null) {
            instance = new SQLGameDAO();
        }
        return instance;
    }
}
