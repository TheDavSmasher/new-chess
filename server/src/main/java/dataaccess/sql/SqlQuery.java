package dataaccess.sql;

import dataaccess.DataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqlQuery<T> {
    T execute(ResultSet resultSet) throws SQLException, DataAccessException;
}
