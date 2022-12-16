package net.pistonmaster.encryptedchat.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ObjectInputStream;
import java.util.List;

public class ChannelDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(in)) {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            out.add(objectInputStream.readObject());
        }
    }
}
