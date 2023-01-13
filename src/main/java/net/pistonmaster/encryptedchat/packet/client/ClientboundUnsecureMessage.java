package net.pistonmaster.encryptedchat.packet.client;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

@Value
@EqualsAndHashCode(callSuper = true)
public class ClientboundUnsecureMessage extends Packet {
    String username;
    String message;
}
