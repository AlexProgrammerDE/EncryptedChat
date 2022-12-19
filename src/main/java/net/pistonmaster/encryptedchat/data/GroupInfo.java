package net.pistonmaster.encryptedchat.data;

import java.io.Serializable;
import java.util.UUID;

public record GroupInfo(UUID groupId, String groupName) implements Serializable {
}
