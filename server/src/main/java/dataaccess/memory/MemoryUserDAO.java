package dataaccess.memory;

import dataaccess.UserDAO;
import model.dataaccess.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {
    static MemoryUserDAO instance;
    private final HashSet<UserData> data;

    MemoryUserDAO() {
        data = new HashSet<>();
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user : data) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }


    @Override
    public UserData getUser(String username, String password) {
        for (UserData user : data) {
            if (user.username().equals(username) && user.password().equals(password)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void createUser(String username, String password, String email) {
        data.add(new UserData(username, password, email));
    }

    @Override
    public void clear() {
        data.clear();
    }

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new MemoryUserDAO();
        }
        return instance;
    }
}
