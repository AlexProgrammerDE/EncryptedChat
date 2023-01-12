package net.pistonmaster.encryptedchat.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.EncryptedChat;
import net.pistonmaster.encryptedchat.crypto.CryptoRSAUtils;
import net.pistonmaster.encryptedchat.packet.server.ServerboundLogin;
import net.pistonmaster.encryptedchat.util.BusHelper;
import net.pistonmaster.encryptedchat.packet.Packet;

@RequiredArgsConstructor
public class ChatClientHandler extends ChannelInboundHandlerAdapter {
    private final ClientMain clientMain;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Received: " + msg);
        if (msg instanceof Packet packet) {
            BusHelper.handlePacket(packet, clientMain.getBus());
        } else throw new RuntimeException("Packet is not of type Packet!");
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Server channel active! Starting login...");
        ctx.channel().write(new ServerboundLogin(clientMain.getUsername(), clientMain.getPair().getPublic(),
                CryptoRSAUtils.sign(EncryptedChat.SIGNATURE_VALUE, clientMain.getPair().getPrivate())));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
