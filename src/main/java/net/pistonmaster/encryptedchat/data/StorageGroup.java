package net.pistonmaster.encryptedchat.data;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record StorageGroup(String groupName, UUID groupId, UUID groupKeyId, List<StorageMember> members, List<StorageMessage> encryptedMessages) {
}
