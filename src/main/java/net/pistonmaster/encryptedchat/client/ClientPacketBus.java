package net.pistonmaster.encryptedchat.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.EncryptedChat;
import net.pistonmaster.encryptedchat.crypto.CryptoAESUtils;
import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.data.GroupInfo;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.packet.client.*;

import javax.crypto.SecretKey;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@SuppressWarnings("unused")
public class ClientPacketBus {
    private final ClientMain clientMain;
    private UUID clientUserId;
    private GroupInfo groupInfo;
    private SecretKey groupSecretKey;

    public void handle(ClientboundGroupJoin packet) {
        groupInfo = packet.getGroupInfo();
        clientMain.getConsoleInput().setPrefixInfo("[" + groupInfo.groupName() + "] ");
        groupSecretKey = CryptoStorage.loadAESKey(EncryptedChat.CLIENT_PATH.resolve(groupInfo.groupKeyId().toString() + ".aeskey"));

        System.out.printf("Joined group %s with id %s", groupInfo.groupName(), groupInfo.groupId());
    }

    public void handle(ClientboundGroupLeave packet) {
        System.out.printf("Left group %s with id %s", groupInfo.groupName(), groupInfo.groupId());
        groupInfo = null;
        groupSecretKey = null;
        clientMain.getConsoleInput().setPrefixInfo("");
    }

    public void handle(ClientboundGroupMessage packet) {
        // TODO: Verify message signature

        System.out.printf("[%s] %s", packet.getMessengerUsername(), CryptoAESUtils.decrypt(packet.getEncryptedMessage(), groupSecretKey));
    }

    public void handle(ClientboundSystemMessage packet) {
        System.out.printf("[SYSTEM] %s", packet.getMessage());
    }

    public void handle(ClientboundDisconnect packet) {
        System.out.printf("Disconnected from server: %s", packet.getReason());
        clientMain.getShutdownExecutor().submit(() -> {
            System.out.println("Shutting down...");
            clientMain.shutdown();
            System.exit(0);
        });
    }

    public void handle(ClientboundUserAnnounce packet) {
        StorageUser user = new StorageUser(packet.getUsername(), packet.getUserId(), packet.getPublicKey());
        clientMain.getKnownUsers().add(user);
        System.out.println("Received user data for " + packet.getUsername());
    }

    public void handle(ClientboundUnsecureMessage packet) {
        System.out.printf("Received unsecure message: %s (%s)", packet.getMessage(), packet.getUsername());
    }

    public void handle(ClientboundLogin packet) {
        clientUserId = packet.getUserId();
        System.out.printf("Logged in with UUID %s", clientUserId);
    }
}
