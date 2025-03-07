package dataaccess.sql;

import dataaccess.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlQuery<T> {
    T execute(PreparedStatement statement) throws SQLException, DataAccessException;
}
