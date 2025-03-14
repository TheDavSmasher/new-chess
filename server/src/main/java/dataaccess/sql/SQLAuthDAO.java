package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.dataaccess.AuthData;

import java.util.UUID;

public class SQLAuthDAO extends SQLDAO implements AuthDAO {
    private static SQLAuthDAO instance;

    public SQLAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return tryQuery("SELECT * FROM auth WHERE authToken=?", rs -> {
            String authToken = rs.getString("authToken");
            String name = rs.getString("username");
            return new AuthData(name, authToken);
        }, token);
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        tryUpdate("INSERT INTO auth (authToken, username) VALUES (?, ?)", SQLDAO::confirmUpdate, token, username);
        return new AuthData(username, token);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        tryUpdate("DELETE FROM auth WHERE authToken=?", SQLDAO::confirmUpdate, token);
    }

    @Override
    public void clear() throws DataAccessException {
        tryUpdate("TRUNCATE auth", SQLDAO::cleared);
    }

    public static AuthDAO getInstance() throws DataAccessException {
        return instance == null ? (instance = new SQLAuthDAO()) : instance;
    }
}
