package net.pistonmaster.encryptedchat.packet.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

@Value
@EqualsAndHashCode(callSuper = true)
public class ServerboundGroupJoin extends Packet {
    String groupName;
}
