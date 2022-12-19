package net.pistonmaster.encryptedchat.packet.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.security.PublicKey;

@Value
@EqualsAndHashCode(callSuper = false)
public class ServerboundGroupLeave extends Packet {
}
