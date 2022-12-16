package net.pistonmaster.encryptedchat.data;

import java.security.PublicKey;
import java.util.UUID;

public record StorageUser(String username, UUID userId, PublicKey userKey) {
}
