package dataaccess;

import dataaccess.sql.SQLAuthDAO;
import model.dataaccess.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SQLAuthDAOTest {

    AuthDAO authDAO;
    String username = "davhig22";

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = SQLAuthDAO.getInstance();
        authDAO.clear();
    }

    @Test
    void getAuthTest() throws DataAccessException {
        String token = authDAO.createAuth(username).authToken();

        Assertions.assertEquals(new AuthData(username, token), authDAO.getAuth(token));

        token = authDAO.createAuth(username).authToken(); //Non-unique username in database

        Assertions.assertEquals(new AuthData(username, token), authDAO.getAuth(token));
    }

    @Test
    void getAuthFail() throws DataAccessException {
        Assertions.assertNull(authDAO.getAuth(null));
        Assertions.assertNull(authDAO.getAuth("not-an-auth-token"));
    }

    @Test
    void createAuthTest() throws DataAccessException {
        AuthData authData = authDAO.createAuth(username);
        Assertions.assertEquals(username, authData.username());
        Assertions.assertNotNull(authData.authToken());
    }

    @Test
    void createAuthFail() {
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
    }

    @Test
    void deleteAuthTest() throws DataAccessException {
        String token = authDAO.createAuth(username).authToken();
        Assertions.assertDoesNotThrow(() -> authDAO.deleteAuth(token));
    }

    @Test
    void deleteAuthFail() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(null));
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("not-an-auth-token"));

        String token = authDAO.createAuth(username).authToken();
        authDAO.deleteAuth(token);

        Assertions.assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(token));
    }

    @Test
    void clear() throws DataAccessException {
        authDAO.createAuth(username);

        Assertions.assertDoesNotThrow(() -> authDAO.clear());
        Assertions.assertDoesNotThrow(() -> authDAO.clear()); // Multiple clears, clear when empty
    }
}