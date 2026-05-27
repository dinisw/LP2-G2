package DAL;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {

    private String serverName;
    private String databaseName;
    private String username;
    private String password;
    private Connection connection;

    public DatabaseConnection() {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        this.serverName = dotenv.get("DB_SERVER");
        this.databaseName = dotenv.get("DB_DATABASE");
        this.username = dotenv.get("DB_USER");
        this.password = dotenv.get("DB_PASSWORD");
    }

    private Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String connectionUrl = "jdbc:sqlserver://" + serverName +
                        ";databaseName=" + databaseName +
                        ";user=" + username +
                        ";password=" + password +
                        ";encryption=true;trustServerCertificate=true";
                connection = DriverManager.getConnection(connectionUrl);
            }
            return connection;
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        return null;
    }

    private boolean disconnect() {
        boolean disconnected = false;

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                disconnected = true;
            }
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        return disconnected;
    }

    private boolean beginTransaction() {
        boolean isTransactionActive = false;

        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(false);
                isTransactionActive = true;
            }
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        return isTransactionActive;
    }

    private boolean commitTransaction() {
        boolean isTransactionActive = false;

        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                isTransactionActive = true;
            }
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        return isTransactionActive;
    }

    private boolean rollbackTransaction() {
        boolean isTransactionClosed = false;

        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                isTransactionClosed = true;
            }
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        return isTransactionClosed;
    }

    public <T> ArrayList<T> select(String sql, RowMapper<T> mapper, Object... params) {
        ArrayList<T> results = new ArrayList<>();

        try {
            connect();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        finally {
            disconnect();
        }
        return results;
    }

    public int create(String sql, Object... params) {
        int result = 0;
        try {
            connect();
            beginTransaction();
            try (PreparedStatement stmt = connect().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                }
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        result = generatedKeys.getInt(1);
                    }
                    else {
                        //Creation failed, no ID obtained.
                        result = 0;
                    }
                }
                commitTransaction();
            }
        }
        catch (SQLException e) {
            rollbackTransaction();
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
        finally {
            disconnect();
        }
        return result;
    }
}
