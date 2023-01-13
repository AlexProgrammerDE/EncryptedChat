package net.pistonmaster.encryptedchat.packet.client;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.security.PublicKey;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class ClientboundUserAnnounce extends Packet {
    UUID requestId;
    String username;
    UUID userId;
    PublicKey publicKey;
}
