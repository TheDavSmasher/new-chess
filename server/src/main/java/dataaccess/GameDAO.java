package dataaccess;

import dataaccess.memory.MemoryGameDAO;
import dataaccess.sql.SQLGameDAO;
import model.dataaccess.GameData;
import service.Service;

import java.util.ArrayList;

public interface GameDAO {
    ArrayList<GameData> listGames() throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    GameData createGame(String gameName) throws DataAccessException;
    void updateGamePlayer(int gameID, String color, String username) throws DataAccessException;
    void updateGameBoard(int gameID, String gameJson) throws DataAccessException;
    void clear() throws DataAccessException;
    static GameDAO getInstance() throws DataAccessException {
        if (Service.UseSQL) {
            return SQLGameDAO.getInstance();
        }
        return MemoryGameDAO.getInstance();
    }
}
