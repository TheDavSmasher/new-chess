package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.dataaccess.AuthData;

import java.sql.ResultSet;
import java.util.UUID;

public class SQLAuthDAO extends SQLDAO implements AuthDAO {
    private static SQLAuthDAO instance;

    public SQLAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return tryStatement("SELECT * FROM auth WHERE authToken=?", preparedStatement -> {
            preparedStatement.setString(1, token);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) return null;
                String authToken = rs.getString("authToken");
                String name = rs.getString("username");
                return new AuthData(name, authToken);
            }
        });
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        return tryStatement("INSERT INTO auth (authToken, username) VALUES (?, ?)", preparedStatement -> {
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, username);

            if (preparedStatement.executeUpdate() == 0) {
                throw new DataAccessException("Did not create any auth");
            }

            return new AuthData(username, token);
        });
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        tryStatement("DELETE FROM auth WHERE authToken=?", preparedStatement -> {
            preparedStatement.setString(1, token);

            if (preparedStatement.executeUpdate() == 0) {
                throw new DataAccessException("Did not delete any auth");
            }
        });
    }

    @Override
    public void clear() throws DataAccessException {
        tryStatement("TRUNCATE auth", preparedStatement -> {
            preparedStatement.executeUpdate();
        });
    }

    public static AuthDAO getInstance() throws DataAccessException {
        if (instance == null) {
            instance = new SQLAuthDAO();
        }
        return instance;
    }
}
