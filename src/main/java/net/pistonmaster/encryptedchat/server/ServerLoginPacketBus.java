package net.pistonmaster.encryptedchat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.EncryptedChat;
import net.pistonmaster.encryptedchat.crypto.CryptoRSAUtils;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.packet.client.ClientboundDisconnect;
import net.pistonmaster.encryptedchat.packet.client.ClientboundLogin;
import net.pistonmaster.encryptedchat.packet.server.ServerboundLogin;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ServerLoginPacketBus {
    private final ServerMain serverMain;
    private final ChannelId channelId;

    public void handle(ServerboundLogin packet) {
        Channel channel = serverMain.getChannels().find(channelId);

        if (serverMain.getConnectedUsers().stream().anyMatch(user -> user.username().equals(packet.getUsername()))) {
            channel.writeAndFlush(new ClientboundDisconnect("User already online!"));
            channel.disconnect();
        }

        StorageUser user = serverMain.getStorage().getUser(packet.getUsername());

        if (user == null) {
            user = serverMain.getStorage().generateUser(packet.getUsername(), packet.getClientKey());
        }

        if (!CryptoRSAUtils.verify(EncryptedChat.SIGNATURE_VALUE, packet.getSignature(), user.userKey())) {
            channel.writeAndFlush(new ClientboundDisconnect("Invalid signature!"));
            channel.disconnect();
            return;
        }

        serverMain.getChannelStates().put(channelId, new ConnectionState(ConnectionState.State.CONNECTED, new ServerPacketBus(serverMain,
                channelId, user)));

        channel.writeAndFlush(new ClientboundLogin(user.userId()));
    }
}
