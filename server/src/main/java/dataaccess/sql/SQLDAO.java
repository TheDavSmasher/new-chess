package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLDAO {
    private static boolean databaseConfigured = false;


    protected static <T> T tryStatement(@Language("SQL") String sql, SqlQuery<T> query, Object... params) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParams(statement, params);
                return query.execute(statement);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected static void tryStatement(@Language("SQL") String sql, SqlUpdate update, Object... params) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParams(statement, params);
                update.execute(statement);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private static void setParams(PreparedStatement statement, Object[] params) throws SQLException {
        int i = 1;
        for (Object param : params) {
            switch (param) {
                case String s -> statement.setString(i++, s);
                case Integer s -> statement.setInt(i++, s);
                default -> statement.setString(i++, param.toString()); //shouldn't be needed
            }
        }
    }

    protected static void configureDatabase() throws DataAccessException {
        if (!databaseConfigured) {
            DatabaseManager.configureDatabase();
            databaseConfigured = true;
        }
    }
}
