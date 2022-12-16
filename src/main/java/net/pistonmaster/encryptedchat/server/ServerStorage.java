package net.pistonmaster.encryptedchat.server;

import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.data.StorageUser;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ServerStorage {
    private final ServerMain main;
    private final Connection connection;

    public ServerStorage(ServerMain main) {
        this.main = main;
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:server.sqlite");
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS users " +
                    "(" +
                    "username TEXT NOT NULL UNIQUE PRIMARY KEY," +
                    "id TEXT NOT NULL UNIQUE," +
                    "pubKey TEXT NOT NULL" +
                    ")").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StorageUser generateUser(String username, PublicKey key) {
        UUID uuid = UUID.randomUUID();

        try {
            connection.prepareStatement("INSERT INTO users (username, id, pubKey) VALUES ('" + username + "', '" + uuid + "', '" + CryptoStorage.saveKeyToString(key) + "')").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new StorageUser(username, uuid, key);
    }

    public StorageUser getUser(String username) {
        try {
             ResultSet set = connection.prepareStatement("SELECT * FROM users WHERE username = '" + username + "'").executeQuery();

             if (!set.next()) {
                 return null;
             }

             return new StorageUser(username, UUID.fromString(set.getString("id")), CryptoStorage.loadPublicKey(set.getString("pubKey")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
