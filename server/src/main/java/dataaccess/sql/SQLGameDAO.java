package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.dataaccess.GameData;

import java.sql.ResultSet;
import java.util.ArrayList;

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
            }
        });
        return gameList;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return tryStatement("SELECT * FROM games WHERE gameID =?", preparedStatement -> {
            preparedStatement.setInt(1, gameID);
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
        });
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        ChessGame game = new ChessGame();
        String gameJson = new Gson().toJson(game);
        return tryStatement("INSERT INTO games (gameName, game) VALUES (?, ?)", preparedStatement -> {
            preparedStatement.setString(1, gameName);
            preparedStatement.setString(2, gameJson);

            if (preparedStatement.executeUpdate() == 0) {
                throw new DataAccessException("Did not create any game");
            }

            int id;
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }

            return new GameData(id, gameName, game);
        });
    }

    @Override
    public void updateGamePlayer(int gameID, String color, String username) throws DataAccessException {
        tryStatement("UPDATE games SET "+ (color.equals("WHITE") ? "whiteUsername" : "blackUsername") +"=? WHERE gameID=?",
                preparedStatement -> {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, gameID);

            if (preparedStatement.executeUpdate() == 0) {
                throw new DataAccessException("Did not update any game");
            }
        });
    }

    @Override
    public void updateGameBoard(int gameID, String gameJson) throws DataAccessException {
        tryStatement("UPDATE games SET game=? WHERE gameID=?", preparedStatement -> {
            preparedStatement.setString(1, gameJson);
            preparedStatement.setInt(2, gameID);

            if (preparedStatement.executeUpdate() == 0) {
                throw new DataAccessException("Did not update any game");
            }
        });
    }

    @Override
    public void clear() throws DataAccessException {
        tryStatement("TRUNCATE games", preparedStatement -> {
            preparedStatement.executeUpdate();
        });
    }

    public static GameDAO getInstance() throws DataAccessException {
        if (instance == null) {
            instance = new SQLGameDAO();
        }
        return instance;
    }
}
