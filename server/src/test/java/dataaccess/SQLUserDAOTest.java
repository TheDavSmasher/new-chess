package dataaccess;

import dataaccess.sql.SQLUserDAO;
import model.dataaccess.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

class SQLUserDAOTest {

    UserDAO userDAO;
    String username = "davhig22";
    String password = "pass123";
    String email = "davhig22@byu.edu";

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = SQLUserDAO.getInstance();
        userDAO.clear();
    }
    @Test
    void getUserTest() throws DataAccessException {
        userDAO.createUser(username, password, email);

        UserData normal = userDAO.getUser(username);
        UserData withPassword = userDAO.getUser(username, password);

        Assertions.assertNotNull(normal);
        Assertions.assertNotNull(withPassword);
        Assertions.assertTrue(BCrypt.checkpw(password, normal.password()));
        Assertions.assertTrue(BCrypt.checkpw(password, withPassword.password()));
    }

    @Test
    void getUserFail() throws DataAccessException {
        Assertions.assertNull(userDAO.getUser(username));
        Assertions.assertNull(userDAO.getUser(username, password));
        userDAO.createUser(username, password, email);
        Assertions.assertNull(userDAO.getUser("nonexistent"));
        Assertions.assertNull(userDAO.getUser(username, "wrong-password"));
        Assertions.assertNull(userDAO.getUser("nonexistent", password));
    }

    @Test
    void createUserTest() {
        Assertions.assertDoesNotThrow(() -> userDAO.createUser(username, password, email));
        Assertions.assertDoesNotThrow(() -> userDAO.createUser("different_user", password, email));
    }

    @Test
    void createUserFail() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(null, password, null));
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(null, null, null));
        userDAO.createUser(username, password, email);
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(username, password, email));
    }

    @Test
    void clear() throws DataAccessException {
        userDAO.createUser(username, password, email);

        Assertions.assertDoesNotThrow(() -> userDAO.clear());
        Assertions.assertDoesNotThrow(() -> userDAO.clear()); //Multiple clears
    }
}