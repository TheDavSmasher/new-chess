package dataaccess.sql;

import chess.ChessGame;
import static model.Serializer.*;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.dataaccess.GameData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLGameDAO extends SQLDAO implements GameDAO {
    private static SQLGameDAO instance;

    public SQLGameDAO () throws DataAccessException {}

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        return tryQuery("SELECT gameID, whiteUsername, blackUsername, gameName FROM games", rs -> {
            ArrayList<GameData> gameList = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name = rs.getString("gameName");

                gameList.add(GameData.forList(id, white, black, name));
            }
            return gameList;
        });
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return tryQuery("SELECT * FROM games WHERE gameID =?", rs -> {
            if (!rs.next()) return null;
            int id = rs.getInt("gameID");
            String white = rs.getString("whiteUsername");
            String black = rs.getString("blackUsername");
            String name = rs.getString("gameName");
            String gameJson = rs.getString("game");
            ChessGame game = deserialize(gameJson, ChessGame.class);

            return new GameData(id, white, black, name, game);
        }, gameID);
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        AtomicInteger id = new AtomicInteger();
        tryUpdate("INSERT INTO games (gameName, game) VALUES (?, ?)", updateResKey -> {
            SQLDAO.confirmUpdate(updateResKey);
            id.set(updateResKey);
        }, gameName, serialize(new ChessGame()));
        return GameData.createNew(id.get(), gameName);
    }

    @Override
    public void updateGamePlayer(int gameID, String color, String username) throws DataAccessException {
        tryUpdate("UPDATE games SET "+ (color.equals("WHITE") ? "whiteUsername" : "blackUsername") +"=? WHERE gameID=?",
                SQLDAO::confirmUpdate, username, gameID);
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
        return instance == null ? (instance = new SQLGameDAO()) : instance;
    }
}
