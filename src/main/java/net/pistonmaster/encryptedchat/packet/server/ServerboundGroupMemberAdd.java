package net.pistonmaster.encryptedchat.packet.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class ServerboundGroupMemberAdd extends Packet {
    UUID targetUser;
    UUID groupId;
    String encryptedSecretKey;
}
