package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import org.intellij.lang.annotations.Language;

import java.sql.*;

import static java.sql.Types.NULL;

public abstract class SQLDAO {
    private static boolean databaseConfigured = false;

    protected static <T> T tryQuery(@Language("SQL") String sql, SqlQuery<T> query, Object... params) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParams(statement, params);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) return null;
                    return query.execute(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected static void tryUpdate(@Language("SQL") String sql, SqlUpdate update, Object... params) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParams(statement, params);
                int result = statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next())
                        result = rs.getInt(1);
                }

                update.execute(result);
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
                case null, default -> statement.setNull(i++, NULL);
            }
        }
    }

    protected static void confirmUpdate(int updateResult) throws DataAccessException {
        if (updateResult == 0) {
            throw new DataAccessException("No rows were updated");
        }
    }

    protected static void cleared(int ignored) {}

    protected static void configureDatabase() throws DataAccessException {
        if (!databaseConfigured) {
            DatabaseManager.configureDatabase();
            databaseConfigured = true;
        }
    }
}
