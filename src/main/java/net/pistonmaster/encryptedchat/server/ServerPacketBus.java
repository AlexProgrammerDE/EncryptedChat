package net.pistonmaster.encryptedchat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelMatchers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.EncryptedChat;
import net.pistonmaster.encryptedchat.crypto.CryptoUtils;
import net.pistonmaster.encryptedchat.data.GroupInfo;
import net.pistonmaster.encryptedchat.data.StorageGroup;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.packet.TestMessagePacket;
import net.pistonmaster.encryptedchat.packet.client.*;
import net.pistonmaster.encryptedchat.packet.server.*;

import java.util.UUID;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ServerPacketBus {
    private final ServerMain serverMain;
    private final ChannelId channelId;
    @Getter
    private final StorageUser user;
    private UUID currentGroup;

    public void handle(ServerboundGroupJoin packet) {
        Channel channel = serverMain.getChannels().find(channelId);

        StorageGroup group = serverMain.getStorage().getGroup(packet.getGroupName());

        if (group == null) {
            group = serverMain.getStorage().generateGroup(packet.getGroupName(), user.userId(), packet.getGroupKey());
        } else if (!group.members().contains(user.userId())) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not member of group!"));
            return;
        }

        channel.writeAndFlush(new ClientboundGroupJoin(new GroupInfo(group.groupId(), group.groupName())));
    }

    public void handle(ServerboundGroupLeave packet) {
        currentGroup = null;
        serverMain.getChannels().find(channelId).writeAndFlush(new ClientboundGroupLeave());
    }

    public void handle(ServerboundGroupMemberAdd packet) {

    }

    public void handle(ServerboundGroupMessage packet) {

    }

    public void handle(ServerboundUnsecureMessage packet) {
        System.out.println("Received unsecure message from " + user.username() + ": " + packet.getMessage());
        serverMain.getChannels().writeAndFlush(new ClientboundUnsecureMessage(user.username(), packet.getMessage()),
                ChannelMatchers.isNot(serverMain.getChannels().find(channelId)));
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
