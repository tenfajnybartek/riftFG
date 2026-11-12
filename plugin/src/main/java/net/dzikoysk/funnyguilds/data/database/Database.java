package net.dzikoysk.funnyguilds.data.database;

import com.zaxxer.hikari.HikariDataSource;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.config.sections.MysqlConfiguration;
import net.dzikoysk.funnyguilds.shared.FunnyStringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final ThreadLocal<Connection> TRANSACTION = new ThreadLocal<>();
    private static final ThreadLocal<List<Runnable>> AFTER_COMMIT = new ThreadLocal<>();

    public static Connection currentTransaction() {
        return TRANSACTION.get();
    }

    public static void afterCommit(Runnable action) {
        List<Runnable> callbacks = AFTER_COMMIT.get();
        if (callbacks == null) { action.run(); }
        else { callbacks.add(action); }
    }

    public void transaction(Runnable write) {
        if (TRANSACTION.get() != null) { write.run(); return; }
        try (Connection connection = this.getConnection()) {
            connection.setAutoCommit(false);
            List<Runnable> callbacks = new ArrayList<>();
            TRANSACTION.set(connection);
            AFTER_COMMIT.set(callbacks);
            try {
                write.run();
                connection.commit();
                callbacks.forEach(Runnable::run);
            }
            catch (RuntimeException | SQLException exception) {
                try { connection.rollback(); }
                catch (SQLException rollbackFailure) { exception.addSuppressed(rollbackFailure); }
                throw new IllegalStateException("Database transaction failed; records remain dirty", exception);
            }
            finally {
                TRANSACTION.remove();
                AFTER_COMMIT.remove();
            }
        }
        catch (SQLException exception) {
            throw new IllegalStateException("Cannot open database transaction", exception);
        }
    }

    private final HikariDataSource dataSource;

    public Database() throws ClassNotFoundException {
        Class.forName(FunnyGuilds.getInstance().getPluginConfiguration().dataModel.getJDBCClassName());
        this.dataSource = new HikariDataSource();
        MysqlConfiguration c = FunnyGuilds.getInstance().getPluginConfiguration().mysql;

        int poolSize = c.poolSize;
        if (poolSize <= 0) {
            poolSize = Runtime.getRuntime().availableProcessors() * 2 + 1; // (core_count * 2) + spindle [pattern from PostgreSQL wiki]
        }

        String characterEncoding = c.characterEncoding == null || c.characterEncoding.isEmpty()
                ? ""
                : "&characterEncoding=" + c.characterEncoding;

        this.dataSource.setMaximumPoolSize(poolSize);
        this.dataSource.setConnectionTimeout(c.connectionTimeout);

        this.dataSource.setJdbcUrl("jdbc:mariadb://" + c.hostname + ":" + c.port + "/" + c.database + "?useSSL=" + c.useSSL + characterEncoding);
        this.dataSource.setUsername(c.user);

        if (!FunnyStringUtils.isEmpty(c.password)) {
            this.dataSource.setPassword(c.password);
        }

        this.dataSource.addDataSourceProperty("cachePrepStmts", true);
        this.dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        this.dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.dataSource.addDataSourceProperty("useServerPrepStmts", true);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void shutdown() {
        this.dataSource.close();
    }

}
