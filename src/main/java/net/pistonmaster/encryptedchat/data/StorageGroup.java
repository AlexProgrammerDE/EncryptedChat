package net.pistonmaster.encryptedchat.data;

import java.util.Set;
import java.util.UUID;

public record StorageGroup(String groupName, UUID groupId, UUID owner, Set<UUID> members, Set<String> encryptedMessages) {
}
