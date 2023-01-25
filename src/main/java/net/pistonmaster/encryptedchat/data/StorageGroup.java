package net.pistonmaster.encryptedchat.data;

import java.util.List;
import java.util.UUID;

public record StorageGroup(String groupName, UUID groupId, UUID groupKeyId, List<StorageMember> members,
                           List<StorageMessage> encryptedMessages) {
}
