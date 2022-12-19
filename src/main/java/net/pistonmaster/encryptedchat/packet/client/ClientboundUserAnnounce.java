package net.pistonmaster.encryptedchat.packet.client;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.security.PublicKey;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = false)
public class ClientboundUserAnnounce extends Packet {
    String username;
    UUID userId;
    PublicKey publicKey;
}