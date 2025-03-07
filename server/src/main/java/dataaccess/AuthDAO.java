package dataaccess;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.sql.SQLAuthDAO;
import model.dataaccess.AuthData;
import service.Service;

public interface AuthDAO {
    AuthData getAuth(String token) throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;
    void clear() throws DataAccessException;
    static AuthDAO getInstance() throws DataAccessException {
        if (Service.UseSQL) {
            return SQLAuthDAO.getInstance();
        }
        return MemoryAuthDAO.getInstance();
    }
}
