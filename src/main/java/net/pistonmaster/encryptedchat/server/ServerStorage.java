package net.pistonmaster.encryptedchat.server;

import com.sun.nio.sctp.MessageInfo;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.data.StorageGroup;
import net.pistonmaster.encryptedchat.data.StorageMessage;
import net.pistonmaster.encryptedchat.data.StorageUser;
import org.jooq.Record;

import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import static net.pistonmaster.encryptedchat.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

@RequiredArgsConstructor
public class ServerStorage {
    private final ServerMain main;

    public StorageUser generateUser(String username, PublicKey key) {
        UUID uuid = UUID.randomUUID();

        insertInto(USERS)
                .columns(USERS.NAME, USERS.ID, USERS.PUBLIC_KEY)
                .values(username, uuid.toString(), CryptoStorage.saveKeyToString(key))
                .execute();

        return new StorageUser(username, uuid, key);
    }

    public StorageUser getUser(String username) {
        Record record = selectFrom(USERS)
                .where(USERS.NAME.eq(username))
                .fetchOne();

        if (record == null) {
            return null;
        }

        return new StorageUser(username,
                UUID.fromString(record.get(USERS.ID)),
                CryptoStorage.loadPublicKey(record.get(USERS.PUBLIC_KEY)));
    }

    public StorageGroup generateGroup(String groupName, UUID owner, PublicKey groupKey) {
        UUID uuid = UUID.randomUUID();

        insertInto(GROUPS)
                .columns(GROUPS.ID, GROUPS.NAME, GROUPS.OWNER, GROUPS.PUBLIC_KEY)
                .values(uuid.toString(), groupName, owner.toString(), CryptoStorage.saveKeyToString(groupKey))
                .execute();

        insertInto(GROUP_MEMBERS)
                .columns(GROUP_MEMBERS.GROUP_ID, GROUP_MEMBERS.USER_ID)
                .values(uuid.toString(), owner.toString())
                .execute();

        return new StorageGroup(groupName, uuid, List.of(owner), List.of());
    }

    public StorageGroup getGroup(String groupName) {
        Record record = selectFrom(GROUPS)
                .where(GROUPS.NAME.equalIgnoreCase(groupName))
                .fetchOne();

        if (record == null) {
            return null;
        }

        List<UUID> members = selectFrom(GROUP_MEMBERS)
                .where(GROUP_MEMBERS.GROUP_ID.eq(record.get(GROUPS.ID)))
                .fetch()
                .map(r -> UUID.fromString(r.get(GROUP_MEMBERS.USER_ID)));

        List<StorageMessage> messages = selectFrom(GROUP_MESSAGES)
                .where(GROUP_MESSAGES.GROUP_ID.eq(record.get(GROUPS.ID)))
                .fetch()
                .map(r -> new StorageMessage(UUID.fromString(r.get(GROUP_MESSAGES.USER_ID)), r.get(GROUP_MESSAGES.MESSAGE)));

        return new StorageGroup(record.get(GROUPS.NAME),
                UUID.fromString(record.get(GROUPS.ID)),
                members,
                messages);
    }
}
