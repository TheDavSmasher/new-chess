package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.dataaccess.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;

public class SQLUserDAO extends SQLDAO implements UserDAO {
    private static SQLUserDAO instance;

    public SQLUserDAO () throws DataAccessException {
        configureDatabase();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return tryStatement("SELECT * FROM users WHERE username =?", preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) return null;
                String name = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                return new UserData(name, password, email);
            }
        }, username);
    }

    @Override
    public UserData getUser(String username, String password) throws DataAccessException {
        UserData userData = getUser(username);
        if (userData == null) return null;
        String storedPassword = userData.password();

        if (!BCrypt.checkpw(password, storedPassword)) return null;
        return userData;
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        tryUpdate("INSERT INTO users (username, password, email) VALUES (?, ?, ?)", resultRes -> {
            if (resultRes == 0) {
                throw new DataAccessException("Did not create any user");
            }
        }, username, hashedPassword, email);
    }

    @Override
    public void clear() throws DataAccessException {
        tryUpdate("TRUNCATE users", SQLDAO::cleared);
    }

    public static UserDAO getInstance() throws DataAccessException {
        if (instance == null) {
            instance = new SQLUserDAO();
        }
        return instance;
    }
}
