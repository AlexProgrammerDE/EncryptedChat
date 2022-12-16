package net.pistonmaster.encryptedchat.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.pistonmaster.encryptedchat.packet.Packet;

import java.io.*;

public class ChannelEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (!(msg instanceof Packet)) {
            throw new IllegalArgumentException("Cannot serialize " + msg.getClass().getName());
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream dataOutputStream = new ObjectOutputStream(byteOut);
        dataOutputStream.writeObject(msg);
        out.writeBytes(byteOut.toByteArray());
    }
}
