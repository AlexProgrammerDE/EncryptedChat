package net.pistonmaster.encryptedchat.packet.client;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class ClientboundLogin extends Packet {
    UUID userId;
}
