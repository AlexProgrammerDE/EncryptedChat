package net.pistonmaster.encryptedchat.packet.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = false)
public class ServerboundUserDataRequest extends Packet {
    // One of both can be null
    String username;
    UUID userId;
}
