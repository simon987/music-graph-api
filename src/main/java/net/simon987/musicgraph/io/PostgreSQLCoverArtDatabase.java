package net.simon987.musicgraph.io;

import net.simon987.musicgraph.logging.LogManager;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.sql.*;
import java.util.logging.Logger;

public class PostgreSQLCoverArtDatabase extends AbstractBinder implements ICoverArtDatabase {

    private String url;
    private String username;
    private String password;

    private Connection connection;

    private static final Logger logger = LogManager.getLogger();

    public PostgreSQLCoverArtDatabase(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    protected void configure() {
        bind(this).to(ICoverArtDatabase.class);
    }

    public byte[] getCover(String mbid) throws SQLException {

        try {
            setupConn();

            PreparedStatement stmt = connection.prepareStatement("SELECT tn FROM mg.covers WHERE mbid::text=?");
            stmt.setString(1, mbid);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] bytes = rs.getBytes(1);
                rs.close();
                return bytes;
            } else {
                rs.close();
                return null;
            }

        } catch (SQLException e) {
            logger.severe(String.format("Exception during cover art query mbid=%s ex=%s",
                    mbid, e.getMessage()));
            throw e;
        }
    }

    private void setupConn() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                logger.fine("Connecting to cover art DB");
                connection = DriverManager.getConnection("jdbc:postgresql://" + url, username, password);
            }
        } catch (SQLException e) {
            logger.fine("Connecting to cover art DB");
            connection = DriverManager.getConnection("jdbc:postgresql://" + url, username, password);
        }
    }
}
