package net.pistonmaster.encryptedchat.packet.client;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.data.GroupInfo;
import net.pistonmaster.encryptedchat.packet.Packet;

@Value
@EqualsAndHashCode(callSuper = true)
public class ClientboundGroupAdd extends Packet {
    GroupInfo groupInfo;
    String encryptedSecretKey;
}
