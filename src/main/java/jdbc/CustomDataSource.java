package jdbc;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@Setter
public final class CustomDataSource implements DataSource {
    public static final String DB_URL = "postgres.url";
    public static final String DB_LOGIN = "postgres.name";
    public static final String DB_PASSWORD = "postgres.password";
    public static final String DB_DRIVER = "postgres.driver";
    private static final Properties properties = new Properties();
    private static final CustomConnector customConnector = new CustomConnector();
    private static volatile CustomDataSource instance;
    private final String driver;
    private final String url;
    private final String name;
    private final String password;

    private CustomDataSource(String driver, String url, String password, String name) {
        this.driver = driver;
        this.url = url;
        this.password = password;
        this.name = name;
    }

    @SneakyThrows
    public static CustomDataSource getInstance() {
        if (instance != null) {
            return instance;
        }

        properties.load(CustomDataSource.class.getClassLoader().getResourceAsStream("app.properties"));
        synchronized (CustomDataSource.class) {
            if (instance == null) {
                instance = new CustomDataSource(
                        properties.getProperty(DB_DRIVER),
                        properties.getProperty(DB_URL),
                        properties.getProperty(DB_PASSWORD),
                        properties.getProperty(DB_LOGIN)
                );
                Class.forName(properties.getProperty(DB_DRIVER));
            }
        }
        return instance;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return customConnector.getConnection(url, name, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return customConnector.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}