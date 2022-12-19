package net.pistonmaster.encryptedchat.packet.client;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.data.GroupInfo;
import net.pistonmaster.encryptedchat.packet.Packet;

/**
 * When you are being added to a group. Either on create or by being added by someone else.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class ClientboundGroupAdd extends Packet {
    String encryptedPrivateKey;
}
