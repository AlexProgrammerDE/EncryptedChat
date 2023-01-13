package net.pistonmaster.encryptedchat.packet;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class TestMessagePacket extends Packet {
    String message;
}
