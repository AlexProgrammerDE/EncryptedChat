package net.pistonmaster.encryptedchat.data;

import java.util.UUID;

public record StorageMessage(UUID sender, String encryptedMessage) {
}
