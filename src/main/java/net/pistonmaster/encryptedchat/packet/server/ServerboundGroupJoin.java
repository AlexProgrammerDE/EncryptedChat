package net.pistonmaster.encryptedchat.packet.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.security.PublicKey;

/**
 * May also create a new group.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class ServerboundGroupJoin extends Packet {
    String groupName;
}
