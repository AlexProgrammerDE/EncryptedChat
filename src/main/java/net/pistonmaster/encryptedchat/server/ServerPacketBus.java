package net.pistonmaster.encryptedchat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.data.GroupInfo;
import net.pistonmaster.encryptedchat.data.StorageGroup;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.packet.TestMessagePacket;
import net.pistonmaster.encryptedchat.packet.client.*;
import net.pistonmaster.encryptedchat.packet.server.*;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ServerPacketBus {
    private final ServerMain serverMain;
    private final ChannelId channelId;
    @Getter
    private final StorageUser user;
    private GroupInfo currentGroup;

    public void handle(ServerboundGroupCreate packet) {
        Channel channel = serverMain.getChannels().find(channelId);

        StorageGroup group = serverMain.getStorage().getGroup(packet.getGroupName());

        if (group != null) {
            channel.writeAndFlush(new ClientboundSystemMessage("Group with name already exists!"));
            return;
        }

        group = serverMain.getStorage().generateGroup(packet.getGroupName(), user.userId(), packet.getGroupKeyId());

        GroupInfo groupInfo = new GroupInfo(group.groupId(), group.groupName(), group.groupKeyId());
        currentGroup = groupInfo;
        channel.writeAndFlush(new ClientboundGroupJoin(groupInfo));
    }

    public void handle(ServerboundGroupJoin packet) {
        Channel channel = serverMain.getChannels().find(channelId);

        StorageGroup group = serverMain.getStorage().getGroup(packet.getGroupName());

        if (group == null) {
            channel.writeAndFlush(new ClientboundSystemMessage("Group not found!"));
            return;
        } else if (group.members().stream().noneMatch(m -> m.userId().equals(user.userId()))) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not member of group!"));
            return;
        }

        GroupInfo groupInfo = new GroupInfo(group.groupId(), group.groupName(), group.groupKeyId());
        currentGroup = groupInfo;
        channel.writeAndFlush(new ClientboundGroupJoin(groupInfo));
    }

    public void handle(ServerboundGroupLeave packet) {
        currentGroup = null;
        serverMain.getChannels().find(channelId).writeAndFlush(new ClientboundGroupLeave());
    }

    public void handle(ServerboundGroupMemberAdd packet) {
        Channel channel = serverMain.getChannels().find(channelId);

        if (currentGroup == null) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not in a group!"));
            return;
        }

        StorageGroup group = serverMain.getStorage().getGroup(currentGroup.groupId());

        if (group == null) {
            channel.writeAndFlush(new ClientboundSystemMessage("Group not found!"));
            return;
        } else if (group.members().stream().noneMatch(m -> m.userId().equals(user.userId()))) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not member of group!"));
            return;
        } else if (group.members().stream().noneMatch(m -> m.userId().equals(user.userId()) && m.admin())) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not admin of group!"));
            return;
        } else if (group.members().stream().anyMatch(m -> m.userId().equals(packet.getTargetUser()))) {
            channel.writeAndFlush(new ClientboundSystemMessage("User is already member of group!"));
            return;
        } else if (serverMain.getStorage().getUser(packet.getTargetUser()) == null) {
            channel.writeAndFlush(new ClientboundSystemMessage("User not found!"));
            return;
        } else if (packet.getTargetUser().equals(user.userId())) {
            channel.writeAndFlush(new ClientboundSystemMessage("You can't add yourself!"));
            return;
        }

        Optional<StorageUser> storageUser = serverMain.getConnectedUsers().stream().filter(user -> user.userId().equals(packet.getTargetUser())).findFirst();
        if (storageUser.isEmpty()) {
            channel.writeAndFlush(new ClientboundSystemMessage("User is not online!"));
            return;
        }

        serverMain.getStorage().addMemberToGroup(group, packet.getTargetUser());
        channel.writeAndFlush(new ClientboundSystemMessage("Added " + packet.getTargetUser() + " to the group!"));

        for (Map.Entry<ChannelId, ConnectionState> entry : serverMain.getChannelStates().entrySet()) {
            if (entry.getValue().getBus() instanceof ServerPacketBus serverPacketBus
                    && serverPacketBus.getUser().userId().equals(packet.getTargetUser())) {
                serverMain.getChannels().find(entry.getKey()).writeAndFlush(new ClientboundGroupAdd(packet.getEncryptedSecretKey()));
            }
        }
    }

    public void handle(ServerboundGroupMessage packet) {
        Channel channel = serverMain.getChannels().find(channelId);
        if (currentGroup == null) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not in a group!"));
            return;
        }

        StorageGroup group = serverMain.getStorage().getGroup(currentGroup.groupId());
        if (group == null) {
            channel.writeAndFlush(new ClientboundSystemMessage("Group not found!"));
            return;
        } else if (group.members().stream().noneMatch(m -> m.userId().equals(user.userId()))) {
            channel.writeAndFlush(new ClientboundSystemMessage("Not member of group!"));
            return;
        }

        serverMain.getChannels().writeAndFlush(new ClientboundGroupMessage(user.userId(), user.username(), packet.getEncryptedMessage()));
    }

    public void handle(ServerboundUnsecureMessage packet) {
        System.out.println("Received unsecure message from " + user.username() + ": " + packet.getMessage());
        serverMain.getChannels().writeAndFlush(new ClientboundUnsecureMessage(user.username(), packet.getMessage()));
    }

    public void handle(ServerboundUserDataRequest packet) {
        StorageUser user = serverMain.getStorage().getUser(packet.getUsername());
        if (user == null) {
            System.out.println("User " + packet.getUsername() + " does not exist!");
            return;
        }

        serverMain.getChannels().find(channelId).writeAndFlush(new ClientboundUserAnnounce(
                packet.getRequestId(), user.username(), user.userId(), user.userKey()));
    }

    public void handle(TestMessagePacket packet) {
        System.out.println("I just handled a packet on the server!");
    }
}
