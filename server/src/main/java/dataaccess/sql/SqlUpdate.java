package dataaccess.sql;

import dataaccess.DataAccessException;

import java.sql.SQLException;

public interface SqlUpdate {
    void execute(int updated) throws SQLException, DataAccessException;
}
