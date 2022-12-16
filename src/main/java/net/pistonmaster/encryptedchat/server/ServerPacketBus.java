package net.pistonmaster.encryptedchat.server;

import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelMatchers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.packet.TestMessagePacket;
import net.pistonmaster.encryptedchat.packet.client.ClientboundUnsecureMessage;
import net.pistonmaster.encryptedchat.packet.client.ClientboundUserAnnounce;
import net.pistonmaster.encryptedchat.packet.server.*;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ServerPacketBus {
    private final ServerMain serverMain;
    private final ChannelId channelId;
    @Getter
    private final StorageUser user;

    public void handle(ServerboundGroupMemberAdd packet) {

    }

    public void handle(ServerboundGroupMessage packet) {

    }

    public void handle(ServerboundUnsecureMessage packet) {
        System.out.println("Received unsecure message from " + user.username() + ": " + packet.getMessage());
        serverMain.getChannels().writeAndFlush(new ClientboundUnsecureMessage(user.username(), packet.getMessage()),
                ChannelMatchers.isNot(serverMain.getChannels().find(channelId)));
    }

    public void handle(ServerboundGroupCreate packet) {

    }

    public void handle(ServerboundUserDataRequest packet) {
        StorageUser user = serverMain.getStorage().getUser(packet.getUsername());
        if (user == null) {
            System.out.println("User " + packet.getUsername() + " does not exist!");
            return;
        }

        serverMain.getChannels().find(channelId).writeAndFlush(new ClientboundUserAnnounce(user.username(), user.userId(), user.userKey()));
    }

    public void handle(TestMessagePacket packet) {
        System.out.println("I just handled a packet on the server!");
    }
}
