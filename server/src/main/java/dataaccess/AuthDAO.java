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
        return Service.UseSQL ? SQLAuthDAO.getInstance() : MemoryAuthDAO.getInstance();
    }
}
