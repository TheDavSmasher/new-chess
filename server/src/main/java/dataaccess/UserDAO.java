package dataaccess;

import dataaccess.memory.MemoryUserDAO;
import dataaccess.sql.SQLUserDAO;
import model.dataaccess.UserData;
import service.Service;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;
    UserData getUser(String username, String password) throws DataAccessException;
    void createUser(String username, String password, String email) throws DataAccessException;
    void clear() throws DataAccessException;
    static UserDAO getInstance() throws DataAccessException {
        if (Service.UseSQL) {
            return SQLUserDAO.getInstance();
        }
        return MemoryUserDAO.getInstance();
    }
}
