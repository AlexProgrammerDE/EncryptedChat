package net.pistonmaster.encryptedchat.client;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.data.GroupInfo;
import net.pistonmaster.encryptedchat.packet.TestMessagePacket;
import net.pistonmaster.encryptedchat.packet.client.*;
import net.pistonmaster.encryptedchat.data.StorageUser;

import java.util.UUID;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ClientPacketBus {
    private final ClientMain clientMain;
    private UUID clientUserId;
    private GroupInfo groupInfo;

    public void handle(ClientboundDisconnect packet) {
        System.out.printf("Disconnected from server: %s", packet.getReason());
        clientMain.getShutdownExecutor().submit(() -> {
            System.out.println("Shutting down...");
            clientMain.shutdown();
            System.exit(0);
        });
    }

    public void handle(ClientboundGroupJoin packet) {
        groupInfo = packet.getGroupInfo();
        clientMain.getConsoleInput().setPrefixInfo(groupInfo.groupName());
        System.out.printf("Joined group %s with id %s", groupInfo.groupName(), groupInfo.groupId());
    }

    public void handle(ClientboundGroupMessage packet) {

    }

    public void handle(ClientboundUserAnnounce packet) {
        clientMain.getKnownUsers().add(new StorageUser(packet.getUsername(), packet.getUserId(), packet.getPublicKey()));
        System.out.println("Received user data for " + packet.getUsername());
    }

    public void handle(ClientboundUnsecureMessage packet) {
        System.out.printf("Received unsecure message: %s (%s)", packet.getMessage(), packet.getUsername());
    }

    public void handle(ClientboundLogin packet) {
        clientUserId = packet.getUserId();
        System.out.printf("Logged in with UUID %s", clientUserId);
    }

    public void handle(TestMessagePacket packet) {
        System.out.println("I just handled a packet on the client!");
    }
}
