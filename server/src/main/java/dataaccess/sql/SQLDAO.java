package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import org.intellij.lang.annotations.Language;

import java.sql.*;

import static java.sql.Types.NULL;

public abstract class SQLDAO {
    private static boolean databaseConfigured = false;

    protected SQLDAO() throws DataAccessException {
        if (!databaseConfigured) {
            DatabaseManager.configureDatabase();
            databaseConfigured = true;
        }
    }

    protected static <T> T tryQuery(@Language("SQL") String sql, SqlQuery<T> query, Object... params) throws DataAccessException {
        try (PreparedStatement statement = getStatement(sql, params); ResultSet rs = statement.executeQuery()) {
            return query.execute(rs);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected static void tryUpdate(@Language("SQL") String sql, SqlUpdate update, Object... params) throws DataAccessException {
        try (PreparedStatement statement = getStatement(sql, params)) {
            int result = statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next())
                    result = rs.getInt(1);
            }

            update.execute(result);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private static PreparedStatement getStatement(String sql, Object... params) throws DataAccessException, SQLException {
        Connection connection = DatabaseManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length;) {
            switch (params[i]) {
                case String s -> statement.setString(++i, s);
                case Integer s -> statement.setInt(++i, s);
                case null, default -> statement.setNull(++i, NULL);
            }
        }
        return statement;
    }

    protected static void confirmUpdate(int updateResult) throws DataAccessException {
        if (updateResult == 0) {
            throw new DataAccessException("No rows were updated");
        }
    }

    protected static void cleared(int ignored) {}
}
