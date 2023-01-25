package net.pistonmaster.encryptedchat.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.util.BusHelper;
import net.pistonmaster.encryptedchat.packet.Packet;
import net.pistonmaster.encryptedchat.util.BusHelper;

import java.util.Objects;

@RequiredArgsConstructor
public class ChatServerHandler extends ChannelInboundHandlerAdapter {
    private final ServerMain serverMain;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Received: " + msg);
        if (msg instanceof Packet packet) {
            BusHelper.handlePacket(packet, getState(ctx.channel().id()).getBus());
        } else throw new RuntimeException("Packet is not of type Packet!");
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client channel active!");
        serverMain.getChannels().add(ctx.channel());
        getState(ctx.channel().id());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("Client connected! " + ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected! " + ctx.channel().remoteAddress());
        // Make sure user is not kept as logged in
        serverMain.getChannelStates().remove(ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private ConnectionState getState(ChannelId id) {
        return serverMain.getChannelStates().compute(id, (k, v) ->
                Objects.requireNonNullElseGet(v, () -> new ConnectionState(ConnectionState.State.LOGIN, new ServerLoginPacketBus(serverMain, k))));
    }
}
