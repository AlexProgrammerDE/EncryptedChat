package net.pistonmaster.encryptedchat.data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record StorageGroup(String groupName, UUID groupId, List<UUID> members, List<StorageMessage> encryptedMessages) {
}