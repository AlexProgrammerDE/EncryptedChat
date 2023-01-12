package net.pistonmaster.encryptedchat.data;

import java.util.UUID;

public record StorageMember(UUID userId, boolean admin) {
}
